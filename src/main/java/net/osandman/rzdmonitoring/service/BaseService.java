package net.osandman.rzdmonitoring.service;

import com.fasterxml.jackson.databind.JsonNode;
import net.osandman.rzdmonitoring.client.RestConnector;
import net.osandman.rzdmonitoring.client.dto.FirstResponse;
import net.osandman.rzdmonitoring.entity.Direction;
import net.osandman.rzdmonitoring.util.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;

import static net.osandman.rzdmonitoring.util.Utils.sleep;

public abstract class BaseService {
    private final String endPoint; //"/timetable/public/ru";

    @Autowired
    @Qualifier("restTemplateConnector")
    protected RestConnector restConnector;

    protected static final Map<String, String> BASE_PARAMS = new HashMap<>() {{
        put("dir", Direction.ONE_WAY.code);
        put("tfl", Train.ALL.value);
        put("checkSeats", "0");
    }};

    public BaseService(String endPoint) {
        this.endPoint = endPoint;
    }

    // TODO перенести метод в RouteService
    protected String getRoutesResponse(Map<String, String> specialParams) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.setAll(specialParams);

        sleep(500);

        String response = restConnector.callGetRequest(endPoint, params);
        JsonNode jsonNode = JsonParser.parse(response);
        // в случае если запрос сразу возвращает конечный результат, судя по тестам это маршруты с билетам без мест
        if (jsonNode.path("result").asText().equalsIgnoreCase("OK")
            && !jsonNode.path("tp").path(0).path("list").isEmpty()) {
            return restConnector.callGetRequest(endPoint, params);
        }
        // запрос может вернуть ответ с сообщением, что поезда не найдены
        if (jsonNode.path("tp").path(0).path("msgList").path(0)
            .path("message").asText().toLowerCase().contains("не найдено ни одного поезда")) {
            return null;
        }

        FirstResponse firstResponse = JsonParser.parse(response, FirstResponse.class);

        sleep(1000);
        params.clear();
        params.add("layer_id", specialParams.get("layer_id"));
        params.add("rid", String.valueOf(firstResponse.getRID()));
        return restConnector.callGetRequest(endPoint, params);
    }
}
