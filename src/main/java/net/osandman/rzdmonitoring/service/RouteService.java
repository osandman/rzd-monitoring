package net.osandman.rzdmonitoring.service;

import net.osandman.rzdmonitoring.client.RequestProcess;
import net.osandman.rzdmonitoring.client.dto.route.RootRoute;
import net.osandman.rzdmonitoring.mapping.RouteMapper;
import net.osandman.rzdmonitoring.util.JsonParser;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RouteService extends BaseService {
    public static final String ROUTE_ENDPOINT = "";
    private final RouteMapper routeMapper;

    public RouteService(RequestProcess requestProcess, RouteMapper routeMapper) {
        super(ROUTE_ENDPOINT, requestProcess);
        this.routeMapper = routeMapper;
    }

    public String findRoutes(String fromStationCode, String toStationCode, String date) {
        Map<String, String> addParams = new HashMap<>() {{
            put("layer_id", "5827"); // код получения списка маршрутов
            put("code0", fromStationCode);
            put("code1", toStationCode);
            put("dt0", date);
        }};
        return getRequestToFindRoutes(addParams);
    }

    private String getRequestToFindRoutes(Map<String, String> specialParams) {
        String bodyFromResponse = getBodyFromResponse(specialParams);
        RootRoute rootRoute = JsonParser.parse(bodyFromResponse, RootRoute.class);
        if (rootRoute != null) {
            if (rootRoute.tp.iterator().next().list.isEmpty()) {
                return "Маршруты не найдены";
            } else {
                return routeMapper.mapping(rootRoute);
            }
        }
        return "Произошла ошибка, обратитесь к администратору";
    }
}
