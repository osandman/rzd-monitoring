package net.osandman.rzdmonitoring.service;

import net.osandman.rzdmonitoring.client.RequestProcess;
import net.osandman.rzdmonitoring.dto.FirstResponse;
import net.osandman.rzdmonitoring.dto.route.RootRoute;
import net.osandman.rzdmonitoring.entity.Station;
import net.osandman.rzdmonitoring.service.printer.ConsolePrinter;
import net.osandman.rzdmonitoring.util.JsonParser;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static net.osandman.rzdmonitoring.util.Utils.sleep;

@Service
public class RzdMonitoringService {

    private final RequestProcess requestProcess;
    private final ConsolePrinter printer;
    private final static String END_POINT = "";

    private final Map<String, String> baseParams = new ConcurrentHashMap<>() {{
        put("dir", "0");
        put("tfl", "3");
        put("checkSeats", "1");
    }};

    public RzdMonitoringService(RequestProcess requestProcess, ConsolePrinter printer) {
        this.requestProcess = requestProcess;
        this.printer = printer;
    }

    public String getRoutes(Station fromStation, Station toStation, String date) {
        baseParams.put("code0", fromStation.code());
        baseParams.put("code1", toStation.code());
        baseParams.put("dt0", date);
        return getRootRoute();
    }

    private String getBodyFromResponse() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.setAll(baseParams);

        sleep(500);
        FirstResponse firstResponse;
        try {
            firstResponse = requestProcess.callGetRequest(END_POINT, params, FirstResponse.class);
            System.out.println(firstResponse);
        } catch (Exception e) {
            // в случае если запрос сразу возвращает конечный результат, судя по тестам это маршруты с билетам без мест
            return requestProcess.callGetRequest(END_POINT, params);
        }

        sleep(1000);
        params.clear();
        params.add("layer_id", baseParams.get("layer_id"));
        params.add("rid", String.valueOf(firstResponse.RID));
        return requestProcess.callGetRequest(END_POINT, params);
    }

    private String getRootRoute() {
        baseParams.put("layer_id", "5827");
        RootRoute rootRoute = JsonParser.parse(getBodyFromResponse(), RootRoute.class);
        if (rootRoute != null) {
            return printer.printRoute(rootRoute);
        }
        return "not found";
    }
}
