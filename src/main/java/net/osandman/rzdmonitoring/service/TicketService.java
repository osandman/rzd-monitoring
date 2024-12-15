package net.osandman.rzdmonitoring.service;

import lombok.extern.slf4j.Slf4j;
import net.osandman.rzdmonitoring.client.dto.FirstResponse;
import net.osandman.rzdmonitoring.client.dto.route.RootRoute;
import net.osandman.rzdmonitoring.client.dto.route.Route;
import net.osandman.rzdmonitoring.client.dto.train.RootTrain;
import net.osandman.rzdmonitoring.dto.TicketsResult;
import net.osandman.rzdmonitoring.dto.TrainDto;
import net.osandman.rzdmonitoring.entity.LayerId;
import net.osandman.rzdmonitoring.mapping.Printer;
import net.osandman.rzdmonitoring.scheduler.ScheduleConfig;
import net.osandman.rzdmonitoring.scheduler.TicketsTask;
import net.osandman.rzdmonitoring.service.notifier.Notifier;
import net.osandman.rzdmonitoring.util.JsonParser;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.osandman.rzdmonitoring.util.Utils.sleep;

@Service
@Slf4j
public class TicketService extends BaseService {

    public static final String TICKETS_ENDPOINT = "";
    private final Printer printer;
    private final RouteService routeService;
    private final Notifier notifier;
    private final ScheduleConfig scheduleConfig;
    private final long pause;

    public TicketService(Printer printer, RouteService routeService, Notifier notifier, ScheduleConfig scheduleConfig) {
        super(TICKETS_ENDPOINT);
        this.printer = printer;
        this.routeService = routeService;
        this.notifier = notifier;
        this.scheduleConfig = scheduleConfig;
        pause = scheduleConfig.getInterval();
    }

    public TicketsResult process(TicketsTask ticketsTask) {
        BASE_PARAMS.put("dt0", ticketsTask.date());
        Long chatId = ticketsTask.chatId();
        notifier.sendMessage(
            "▶ Начинаем поиск билетов для задачи '" + ticketsTask.taskId() + "'",
            chatId
        );
        RootRoute rootRoute = routeService
            .findRootRoute(ticketsTask.fromCode(), ticketsTask.toCode(), ticketsTask.date());
        if (rootRoute == null) {
            String errorMsg = "Ошибка получения маршрутов";
            log.error(errorMsg);
            notifier.sendMessage(errorMsg, chatId);
            return new TicketsResult(0, errorMsg, List.of());
        }
        List<String> routeNumbersToFind = Arrays.asList(ticketsTask.routeNumbers());
        List<String> availableNumbers = rootRoute.getTp().stream()
            .flatMap(route -> route.list.stream().map(r -> r.number))
            .toList();
        String errorMsgRoutesNotFound = "Маршруты с билетами не найдены";
        if (availableNumbers.isEmpty()) {
            log.warn("{}, taskId={}, rootRoute={}", errorMsgRoutesNotFound, ticketsTask.taskId(), rootRoute);
            notifier.sendMessage(errorMsgRoutesNotFound, chatId);
            return new TicketsResult(0, errorMsgRoutesNotFound, List.of());
        }
        List<String> checkedNumbers;
        if (routeNumbersToFind.isEmpty()) { // если пусто, то ищем все маршруты
            checkedNumbers = availableNumbers;
        } else {
            checkedNumbers = routeNumbersToFind.stream()
                .filter(availableNumbers::contains)
                .toList();
        }
        if (checkedNumbers.isEmpty()) {
            String errorMsgSkipSearch = "Поиск не выполнен, таких маршрутов не существует";
            log.warn("{}. Ищем поезда: {}, а найдены поезда: {}", errorMsgSkipSearch, routeNumbersToFind, availableNumbers);
            notifier.sendMessage(errorMsgSkipSearch, chatId);
            return new TicketsResult(0, errorMsgSkipSearch, List.of());
        }
        log.info("Ищем билеты для поездов: {}, taskId={}", checkedNumbers, ticketsTask.taskId());
        notifier.sendMessage("Ищем билеты для поездов: %s".formatted(checkedNumbers), chatId);

        TicketsResult ticketsResult = findTickets(rootRoute, ticketsTask.date(), checkedNumbers, chatId);
        log.info("{}, найдено {} маршрутов", ticketsResult.comment(), ticketsResult.findRoutes());
        log.info("Ожидание до следующего запроса билетов {} минут", scheduleConfig.getInterval());
        return ticketsResult;
    }

    private TicketsResult findTickets(
        @NonNull RootRoute rootRoute,
        @NonNull String date,
        @NonNull List<String> routeNumbers,
        @NonNull Long chatId
    ) {
        if (rootRoute.tp == null) {
            String comment = "rootRoute.tp is null, rootRoute='%s'".formatted(rootRoute);
            log.warn(comment);
            return new TicketsResult(0, comment, List.of());
        }
        int countMatchesRoutes = 0;
        List<TrainDto> trains = new ArrayList<>();
        for (String number : routeNumbers) {

            Route routeToFind = rootRoute.tp.stream()
                .filter(tp -> !tp.list.isEmpty())
                .flatMap(tp -> tp.list.stream())
                .filter(route -> number.equalsIgnoreCase(route.number))
                .findAny()
                .orElse(null);
            log.info("Ищем свободные места для поезда: {}", number);
            countMatchesRoutes++;
            String trainWithTickets;
            try {
                trainWithTickets = findTrainWithTickets(date, routeToFind);
            } catch (Exception e) {
                trains.add(TrainDto.builder()
                    .trainNumber(number)
                    .error(e.getMessage())
                    .build());
                notifier.sendMessage("Ошибка при запросе билетов маршрута %s".formatted(number), chatId);
                continue;
            }
            RootTrain rootTrain = JsonParser.parse(
                trainWithTickets,
                RootTrain.class
            );
            if (rootTrain == null) {
                String error = "Ошибка при разборе ответа от сервера, поезд: %s".formatted(number);
                log.error(error);
                notifier.sendMessage(error, chatId);
                trains.add(TrainDto.builder()
                    .trainNumber(number)
                    .error(error)
                    .build());
                continue;
            }
            trains.add(printer.ticketsMapping(rootTrain, chatId));
        }
//        notifier.sendMessage("Поиск билетов завершен, повтор через %d минут".formatted(scheduleConfig.getInterval()));
        return new TicketsResult(countMatchesRoutes, "Поиск билетов в заданных поездах завершен", trains);
    }

    private String findTrainWithTickets(String date, Route route) {
        MultiValueMap<String, String> firstReqParams = new LinkedMultiValueMap<>();
        firstReqParams.setAll(buildAllParams(date, route));
        sleep(500);
        try {
            String response = restConnector.callGetRequest(TICKETS_ENDPOINT, firstReqParams);
            FirstResponse firstResponse = JsonParser.parse(response, FirstResponse.class);
            log.info("Ответ на запрос RID: '{}'", firstResponse);
            sleep(1000);
            MultiValueMap<String, String> secondReqParams = new LinkedMultiValueMap<>();
            secondReqParams.add("layer_id", LayerId.DETAIL_ID.code);
            secondReqParams.add("rid", String.valueOf(firstResponse.getRID()));
            return restConnector.callGetRequest(TICKETS_ENDPOINT, secondReqParams);
        } catch (Exception e) {
            log.error("Ошибка при получении билетов маршрута, параметры: {}, message='{}'",
                firstReqParams, e.getMessage());
            throw new RuntimeException("Ошибка при получении билетов ='%s'".formatted(e.getMessage()), e);
        }
    }

    private static Map<String, String> buildAllParams(String date, Route route) {
        Map<String, String> requestParams = new HashMap<>() {{
            put("layer_id", LayerId.DETAIL_ID.code); // код получения деталей маршрута
            put("code0", String.valueOf(route.code0));
            put("code1", String.valueOf(route.code1));
            put("tnum0", route.number);
            put("dt0", date);
        }};
        requestParams.putAll(BASE_PARAMS);
        return requestParams;
    }
}
