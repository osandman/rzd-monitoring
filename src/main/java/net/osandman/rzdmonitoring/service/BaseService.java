package net.osandman.rzdmonitoring.service;

import net.osandman.rzdmonitoring.client.RequestProcess;
import net.osandman.rzdmonitoring.client.dto.FirstResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.osandman.rzdmonitoring.util.Utils.sleep;

public abstract class BaseService {
    private final String endPoint; //"/timetable/public/ru";
    protected final RequestProcess requestProcess;

    protected final Map<String, String> baseParams = new HashMap<>() {{
        put("dir", Direction.ONE_WAY.value);
        put("tfl", Train.ALL.value);
        put("checkSeats", "1");
    }};

    public BaseService(String endPoint, RequestProcess requestProcess) {
        this.endPoint = endPoint;
        this.requestProcess = requestProcess;
    }

    protected String getBodyFromResponse(Map<String, String> specialParams) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.setAll(baseParams);
        params.addAll(toMultiValueMap(specialParams));

        sleep(500);
        FirstResponse firstResponse;
        try {
            firstResponse = requestProcess.callGetRequest(endPoint, params, FirstResponse.class);
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
