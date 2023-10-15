package net.osandman.rzdmonitoring.service;

import net.osandman.rzdmonitoring.client.RequestProcess;
import net.osandman.rzdmonitoring.dto.FirstResponse;
import net.osandman.rzdmonitoring.dto.route.RootRoute;
import net.osandman.rzdmonitoring.service.printer.ConsolePrinter;
import net.osandman.rzdmonitoring.util.JsonParser;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.osandman.rzdmonitoring.util.Utils.sleep;

public abstract class BaseService {
    private final String endPoint; //"/timetable/public/ru";
    protected final RequestProcess requestProcess;
    protected final ConsolePrinter printer;

    protected final Map<String, String> baseParams = new HashMap<>() {{
        put("dir", "0");
        put("tfl", "3");
        put("checkSeats", "1");
    }};

    public BaseService(String endPoint, RequestProcess requestProcess, ConsolePrinter printer) {
        this.endPoint = endPoint;
        this.requestProcess = requestProcess;
        this.printer = printer;
    }

    protected String getRootRoute(Map<String, String> specialParams) {
        RootRoute rootRoute = JsonParser.parse(getBodyFromResponse(specialParams), RootRoute.class);
        if (rootRoute != null) {
            return printer.printRoute(rootRoute);
        }
        return "Route not found";
    }

    private String getBodyFromResponse(Map<String, String> specialParams) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.setAll(baseParams);
        params.addAll(toMultiValueMap(specialParams));

        sleep(500);
        FirstResponse firstResponse;
        try {
            firstResponse = requestProcess.callGetRequest(endPoint, params, FirstResponse.class);
            System.out.println(firstResponse);
        } catch (Exception e) {
            // в случае если запрос сразу возвращает конечный результат, судя по тестам это маршруты с билетам без мест
            return requestProcess.callGetRequest(endPoint, params);
        }

        sleep(1000);
        params.clear();
        params.add("layer_id", specialParams.get("layer_id"));
        params.add("rid", String.valueOf(firstResponse.RID));
        return requestProcess.callGetRequest(endPoint, params);
    }

    private MultiValueMap<String, String> toMultiValueMap(Map<String, String> addParams) {
        MultiValueMap<String, String> result = new LinkedMultiValueMap<>();
        addParams.keySet().forEach(key -> result.put(key, List.of(addParams.get(key))));
        return result;
    }
}
