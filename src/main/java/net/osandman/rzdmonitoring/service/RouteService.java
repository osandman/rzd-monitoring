package net.osandman.rzdmonitoring.service;

import net.osandman.rzdmonitoring.client.RequestProcess;
import net.osandman.rzdmonitoring.repository.StationEnum;
import net.osandman.rzdmonitoring.mapping.MapperImpl;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RouteService extends BaseService {
    public static final String ROUTE_ENDPOINT = "";

    public RouteService(RequestProcess requestProcess, MapperImpl printer) {
        super(ROUTE_ENDPOINT, requestProcess, printer);
    }

    public String getRoutes(StationEnum fromStationEnum, StationEnum toStationEnum, String date) {
        Map<String, String> addParams = new HashMap<>() {{
            put("layer_id", "5827"); // код получения списка маршрутов
            put("code0", fromStationEnum.code());
            put("code1", toStationEnum.code());
            put("dt0", date);
        }};
        return getRootRoute(addParams);
    }

    public String getRoutes(String fromStation, String toStation, String date) {
        Map<String, String> addParams = new HashMap<>() {{
            put("layer_id", "5827"); // код получения списка маршрутов
            put("code0", fromStation);
            put("code1", toStation);
            put("dt0", date);
        }};
        return getRootRoute(addParams);
    }
}
