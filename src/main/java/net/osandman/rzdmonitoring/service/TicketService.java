package net.osandman.rzdmonitoring.service;

import lombok.extern.slf4j.Slf4j;
import net.osandman.rzdmonitoring.client.dto.FirstResponse;
import net.osandman.rzdmonitoring.client.dto.route.RootRoute;
import net.osandman.rzdmonitoring.client.dto.route.Route;
import net.osandman.rzdmonitoring.client.dto.route.Tp;
import net.osandman.rzdmonitoring.client.dto.train.RootTrain;
import net.osandman.rzdmonitoring.dto.TicketsResult;
import net.osandman.rzdmonitoring.dto.TrainDto;
import net.osandman.rzdmonitoring.entity.LayerId;
import net.osandman.rzdmonitoring.mapping.Printer;
import net.osandman.rzdmonitoring.scheduler.ScheduleConfig;
import net.osandman.rzdmonitoring.scheduler.TicketsTask;
import net.osandman.rzdmonitoring.service.notifier.Notifier;
import net.osandman.rzdmonitoring.util.JsonParser;
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
        RootRoute rootRoute = routeService
            .findRootRoute(ticketsTask.fromCode(), ticketsTask.toCode(), ticketsTask.date());
        if (rootRoute == null) {
            log.info("Ошибка получения маршрутов. "
                     + "Ожидание до следующего запроса билетов {} минут", scheduleConfig.getInterval());
            return null;
        }
        List<String> routeNumbers = Arrays.asList(ticketsTask.routeNumbers());
        if (routeNumbers.isEmpty()) {
            if (rootRoute.getTp() != null) {
                routeNumbers = rootRoute.getTp().stream()
                    .flatMap(route -> route.list.stream().map(r -> r.number))
                    .toList();
            } else {
                log.error("Не найдены маршруты - Tp=null, taskId={}, rootRoute={}", ticketsTask.taskId(), rootRoute);
                return null;
            }
        }
        log.info("Ищем билеты для поездов: {}, taskId={}", Arrays.asList(routeNumbers), ticketsTask.taskId());
//        notifier.sendMessage("▶ Начинаем поиск билетов для задачи '" + taskId + "'");
        TicketsResult ticketsResult = findTickets(rootRoute, ticketsTask.date(), routeNumbers);
        log.info(ticketsResult.comment());
        if (ticketsResult.findRoutes() == 0) {
            log.warn("Нет поездов соответствующих заданным на дату {}", ticketsTask.date());
            return null;
            // TODO убирать из списка тасок текущую
            // AbstractTelegramCommand.threads.get()
        }
        log.info("Ожидание до следующего запроса билетов {} минут", scheduleConfig.getInterval());
        return ticketsResult;
    }

    private TicketsResult findTickets(RootRoute rootRoute, String date, List<String> routeNumbers) {
        if (rootRoute == null) {
            String rootRouteIsNull = "rootRoute is null";
            log.error(rootRouteIsNull);
            return new TicketsResult(0, rootRouteIsNull, List.of());
        }
        if (rootRoute.tp == null) {
            String comment = "rootRoute.tp is null, rootRoute={}";
            log.warn(comment, rootRoute);
            return new TicketsResult(0, comment, List.of());
        }
        int countMatchesRoutes = 0;
        List<TrainDto> trains = new ArrayList<>();
        for (Tp tp : rootRoute.tp) {
            if (tp.list.isEmpty()) {
                log.info("Нет свободных мест в поезде: '{}'", tp);
                continue;
            }
            for (Route route : tp.list) {
                // TODO сделать проверку что ни один из найденных номеров поездов не соответствует заданным и
                // возврат значения
                if (!routeNumbers.isEmpty() && routeNumbers.contains(route.number)) {
                    log.info("Ищем свободные места для поезда: {}", route.number);
                    countMatchesRoutes++;
                    RootTrain rootTrain = JsonParser.parse(
                        findTrainWithTickets(date, route),
                        RootTrain.class
                    );
                    if (rootTrain == null) {
                        String error = "Ошибка при разборе ответа от сервера, поезд: %s".formatted(route.number);
                        log.error(error);
                        trains.add(TrainDto.builder()
                            .trainNumber(route.number)
                            .error(error)
                            .build());
                        continue;
                    }
                    trains.add(printer.ticketsMapping(rootTrain));
                }
            }
        }
//        notifier.sendMessage("Поиск билетов завершен, повтор через %d минут".formatted(scheduleConfig.getInterval()));
        return new TicketsResult(countMatchesRoutes, "Поиск билетов в заданных поездах завершен", trains);
    }

    private String findTrainWithTickets(String date, Route route) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.setAll(buildAllParams(date, route));
        sleep(500);
        String result = null;
        try {
            String response = restConnector.callGetRequest(TICKETS_ENDPOINT, params);
            FirstResponse firstResponse = JsonParser.parse(response, FirstResponse.class);
            log.info("Ответ на запрос RID: '{}'", firstResponse);
            sleep(1000);
            params.clear();
            params.add("layer_id", LayerId.DETAIL_ID.code);
            params.add("rid", String.valueOf(firstResponse.getRID()));
            result = restConnector.callGetRequest(TICKETS_ENDPOINT, params);
        } catch (Exception e) {
            log.error("Ошибка при получении билетов маршрута, параметры: {}, message='{}'", params, e.getMessage());
        }
        return result;
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
