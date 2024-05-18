package net.osandman.rzdmonitoring.service;

import lombok.extern.slf4j.Slf4j;
import net.osandman.rzdmonitoring.client.RequestProcess;
import net.osandman.rzdmonitoring.client.dto.FirstResponse;
import net.osandman.rzdmonitoring.client.dto.route.RootRoute;
import net.osandman.rzdmonitoring.client.dto.route.Route;
import net.osandman.rzdmonitoring.client.dto.route.Tp;
import net.osandman.rzdmonitoring.client.dto.train.RootTrain;
import net.osandman.rzdmonitoring.entity.LayerId;
import net.osandman.rzdmonitoring.mapping.Printer;
import net.osandman.rzdmonitoring.service.notifier.Notifier;
import net.osandman.rzdmonitoring.util.JsonParser;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static net.osandman.rzdmonitoring.util.Utils.sleep;

@Service
@Slf4j
public class TicketService extends BaseService {

    public static final String TICKETS_ENDPOINT = "";
    public static int pause = 1000 * 300; // 5 минут
    private final Printer printer;
    private final RouteService routeService;
    private final Notifier notifier;

    public TicketService(RequestProcess requestProcess, Printer printer, RouteService routeService, Notifier notifier) {
        super(TICKETS_ENDPOINT, requestProcess);
        this.printer = printer;
        this.routeService = routeService;
        this.notifier = notifier;
    }

    public void autoLoop(String date, String fromCode, String toCode, String... trainNumbers) {
        while (true) {
            BASE_PARAMS.put("dt0", date);
            RootRoute rootRoute = routeService.findRootRoute(fromCode, toCode, date);
            if (rootRoute == null) {
                log.info("Ошибка получения маршрутов. "
                         + "Ожидание до следующего запроса билетов {} минут", pause / 1000 / 60.0);
                sleep(pause);
                continue;
            }
            if (trainNumbers.length == 0) {
                trainNumbers = rootRoute.getTp()
                    .stream().flatMap(route -> route.list.stream().map(r -> r.number)).toArray(String[]::new);
            }
            log.info("Ищем билеты для поездов: {}", Arrays.asList(trainNumbers));
            FindRouteResult findRouteResult = findTickets(rootRoute, date, trainNumbers);
            log.info(findRouteResult.comment);
            if (findRouteResult.findRoutes == 0) {
                log.warn("Нет поездов соответствующих заданным на дату {}", date);
                continue;
            }
            log.info("Ожидание до следующего запроса билетов {} минут", pause / 1000 / 60.0);
            sleep(pause);
        }
    }

    public FindRouteResult findTickets(RootRoute rootRoute, String date, String... routeNumber) {
        if (rootRoute == null) {
            String rootRouteIsNull = "rootRoute is null";
            log.error(rootRouteIsNull);
            return new FindRouteResult(0, rootRouteIsNull);
        }
        if (rootRoute.tp == null) {
            String comment = "rootRoute.tp is null, rootRoute={}";
            log.warn(comment, rootRoute);
            return new FindRouteResult(0, comment);
        }
        int countMatchesRoutes = 0;
        notifier.sendMessage("Начинаем поиск билетов");
        for (Tp tp : rootRoute.tp) {
            if (tp.list.isEmpty()) {
                log.info("Нет свободных мест в поезде: '{}'", tp);
                continue;
            }
            for (Route route : tp.list) {
                // TODO сделать проверку что ни один из найденных номеров поездов не соответствует заданным и
                // возврат значения
                if (routeNumber.length != 0 && Arrays.asList(routeNumber).contains(route.number)) {
                    log.info("Ищем свободные места для поезда: {}", route.number);
                    countMatchesRoutes++;
                    RootTrain rootTrain = JsonParser.parse(
                        findTrainWithTickets(date, route),
                        RootTrain.class
                    );
                    if (rootTrain == null) {
                        log.error("Ошибка при разборе ответа от сервера, поезд: {}", route.number);
                        continue;
                    }
                    printer.ticketsMapping(rootTrain);
                }
            }
        }
        notifier.sendMessage("Поиск билетов завершен, повтор через %.2f минут".formatted(pause / 1000 / 60.0));
        return new FindRouteResult(countMatchesRoutes, "Поиск билетов в заданных поездах завершен");
    }

    public record FindRouteResult(int findRoutes, String comment) {
    }


    private String findTrainWithTickets(String date, Route route) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.setAll(buildAllParams(date, route));
        sleep(500);
        String result = null;
        try {
            FirstResponse firstResponse = requestProcess.callGetRequest(TICKETS_ENDPOINT, params, FirstResponse.class);
            log.info("Ответ на запрос RID: '{}'", firstResponse);
            sleep(1000);
            params.clear();
            params.add("layer_id", LayerId.DETAIL_ID.code);
            params.add("rid", String.valueOf(firstResponse.RID));
            result = requestProcess.callGetRequest(TICKETS_ENDPOINT, params);
        } catch (Exception e) {
            log.error("Ошибка при получении билетов маршрута, параметры: {}", params, e);
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
