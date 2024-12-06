package net.osandman.rzdmonitoring.service;

import lombok.extern.slf4j.Slf4j;
import net.osandman.rzdmonitoring.client.RestConnector;
import net.osandman.rzdmonitoring.client.dto.route.RootRoute;
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

    public RouteService(RestConnector restConnector, RouteMapper routeMapper) {
        super(ROUTE_ENDPOINT, restConnector);
        this.routeMapper = routeMapper;
    }

    public String getPrettyStringRoutes(String fromStationCode, String toStationCode, String date) {
        RootRoute rootRoute = findRootRoute(fromStationCode, toStationCode, date);
        if (rootRoute != null && rootRoute.tp != null) {
            if (rootRoute.tp.iterator().next().list.isEmpty()) {
                return "Маршруты не найдены";
            } else {
                return routeMapper.getPrettyString(rootRoute);
            }
        }
        return "Произошла ошибка, обратитесь к администратору";
    }

    public RootRoute findRootRoute(String fromStationCode, String toStationCode, String date) {
        Map<String, String> allRequestParams = buildAllParams(fromStationCode, toStationCode, date);
        RootRoute rootRoute = null;
        try {
            String bodyFromResponse = getResponse(allRequestParams);
            if (bodyFromResponse != null) {
                rootRoute = JsonParser.parse(bodyFromResponse, RootRoute.class);
            }
        } catch (Exception e) {
            log.error("Произошла ошибка, обратитесь к администратору", e);
        }
        return rootRoute;
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
