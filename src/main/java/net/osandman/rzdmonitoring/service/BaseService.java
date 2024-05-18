package net.osandman.rzdmonitoring.service;

import com.fasterxml.jackson.databind.JsonNode;
import net.osandman.rzdmonitoring.client.RequestProcess;
import net.osandman.rzdmonitoring.client.dto.FirstResponse;
import net.osandman.rzdmonitoring.entity.Direction;
import net.osandman.rzdmonitoring.util.JsonParser;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;

import static net.osandman.rzdmonitoring.util.Utils.sleep;

public abstract class BaseService {
    private final String endPoint; //"/timetable/public/ru";
    protected final RequestProcess requestProcess;

    protected static final Map<String, String> BASE_PARAMS = new HashMap<>() {{
        put("dir", Direction.ONE_WAY.code);
        put("tfl", Train.ALL.value);
        put("checkSeats", "1");
    }};

    public BaseService(String endPoint, RequestProcess requestProcess) {
        this.endPoint = endPoint;
        this.requestProcess = requestProcess;
    }

    protected String getResponse(Map<String, String> specialParams) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.setAll(specialParams);

        sleep(500);

        String response = requestProcess.callGetRequest(endPoint, params);
        JsonNode jsonNode = JsonParser.parse(response);
        // в случае если запрос сразу возвращает конечный результат, судя по тестам это маршруты с билетам без мест
        if (jsonNode.get("result").asText().equalsIgnoreCase("OK")) {
            return requestProcess.callGetRequest(endPoint, params);
        }
        FirstResponse firstResponse = JsonParser.parse(response, FirstResponse.class);

        sleep(1000);
        params.clear();
        params.add("layer_id", specialParams.get("layer_id"));
        params.add("rid", String.valueOf(firstResponse.RID));
        return requestProcess.callGetRequest(endPoint, params);
    }
}
