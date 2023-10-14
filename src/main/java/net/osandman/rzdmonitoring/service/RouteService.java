package net.osandman.rzdmonitoring.service;

import net.osandman.rzdmonitoring.client.RequestProcess;
import net.osandman.rzdmonitoring.entity.Station;
import net.osandman.rzdmonitoring.service.printer.ConsolePrinter;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RouteService extends BaseService {
    public RouteService(RequestProcess requestProcess, ConsolePrinter printer) {
        super(requestProcess, printer);
    }

    public String getRoutes(Station fromStation, Station toStation, String date) {
        Map<String, String> addParams = new HashMap<>() {{
            put("layer_id", "5827"); // код получения списка маршрутов
            put("code0", fromStation.code());
            put("code1", toStation.code());
            put("dt0", date);
        }};
        return getRootRoute(addParams);
    }
}
