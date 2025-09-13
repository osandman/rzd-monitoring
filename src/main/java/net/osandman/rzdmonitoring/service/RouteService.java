package net.osandman.rzdmonitoring.service;

import lombok.extern.slf4j.Slf4j;
import net.osandman.rzdmonitoring.client.dto.route.RootRoute;
import net.osandman.rzdmonitoring.client.dto.route.Tp;
import net.osandman.rzdmonitoring.dto.CheckResult;
import net.osandman.rzdmonitoring.entity.LayerId;
import net.osandman.rzdmonitoring.mapping.RouteMapper;
import net.osandman.rzdmonitoring.util.JsonParser;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class RouteService extends BaseService {
    public static final String ROUTE_ENDPOINT = "";
    private final RouteMapper routeMapper;

    public RouteService(RouteMapper routeMapper) {
        super(ROUTE_ENDPOINT);
        this.routeMapper = routeMapper;
    }

    public String getPrettyStringRoutes(String fromStationCode, String toStationCode, String date) {
        RootRoute rootRoute = findRootRoute(fromStationCode, toStationCode, date);
        if (rootRoute == null || rootRoute.tp == null || rootRoute.tp.isEmpty()) {
            return "Маршруты не найдены";
        }
        Tp tp = rootRoute.tp.get(0);
        long countNotTrain = tp.msgList.stream()
            .filter(map -> map.get("message").toLowerCase().contains("в указанную дату поезд не ходит"))
            .count();
        if (countNotTrain >= tp.list.size()) {
            return "В указанную дату поезд не ходит";
        }
        try {
            return routeMapper.getPrettyString(rootRoute);
        } catch (Exception e) {
            return "Произошла ошибка, обратитесь к администратору";
        }
    }

    public RootRoute findRootRoute(String fromStationCode, String toStationCode, String date) {
        Map<String, String> allRequestParams = buildAllParams(fromStationCode, toStationCode, date);
        RootRoute rootRoute = null;
        try {
            String bodyFromResponse = getRoutesResponse(allRequestParams);
            if (bodyFromResponse != null) {
                rootRoute = JsonParser.parse(bodyFromResponse, RootRoute.class);
            }
        } catch (Exception e) {
            log.error("Произошла ошибка, обратитесь к администратору", e);
        }
        return rootRoute;
    }

    public CheckResult checkRoute(RootRoute rootRoute) {
        // проверка того, что маршрут существует, только после этого добавлять задание
        if (rootRoute == null || rootRoute.tp == null || rootRoute.tp.isEmpty()) { // || rootRoute.tp.get(0).list.isEmpty()
            return new CheckResult(false, "Не найден маршрут или ошибка запроса");
        }
        if (rootRoute.result != null && rootRoute.result.toLowerCase().contains("fail")) {
            return new CheckResult(false, "Ошибка запроса");
        }
        Tp tp = rootRoute.tp.get(0);
        if (!tp.list.isEmpty()) {
            long countType = tp.list.stream()
                .filter(route -> route.type == 1) // 1 - пригородные, 0 - междугородние
                .count();
            if (countType >= tp.list.size()) {
                return new CheckResult(false, "Найдены только пригородные поезда");
            }
        }
        long countNotTrain = tp.msgList.stream()
            .filter(map -> map.get("message").toLowerCase().contains("в указанную дату поезд не ходит"))
            .count();
        if (countNotTrain >= tp.list.size()) {
            return new CheckResult(false, "В указанную дату поезд не ходит");
        }
        return new CheckResult(true, "Маршрут(ы) найден(ы)");
    }

    private static Map<String, String> buildAllParams(String fromStationCode, String toStationCode, String date) {
        Map<String, String> requestParams = new HashMap<>() {{
            put("layer_id", LayerId.ROUTE_ID.code); // код получения списка маршрутов
            put("code0", fromStationCode);
            put("code1", toStationCode);
            put("dt0", date);
        }};
        requestParams.putAll(BASE_PARAMS);
        return requestParams;
    }
}
