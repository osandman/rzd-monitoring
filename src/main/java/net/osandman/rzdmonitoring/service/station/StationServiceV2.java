package net.osandman.rzdmonitoring.service.station;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import net.osandman.rzdmonitoring.client.RestConnector;
import net.osandman.rzdmonitoring.dto.station.StationDto;
import net.osandman.rzdmonitoring.dto.station.StationDtoV2;
import net.osandman.rzdmonitoring.util.Utils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.util.StringUtils.hasText;

@Service
@RequiredArgsConstructor
@Primary
public class StationServiceV2 implements StationService {

    public static final String STATION_URL = "https://ticket.rzd.ru";

    private final RestConnector restConnector;

    @Override
    public List<StationDto> findStations(String partName) {
        String lang = Utils.detectLang(partName);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>() {{
            put("GroupResults", List.of("true"));
            put("Query", List.of(partName));
            put("Language", List.of(lang));
        }};
        ObjectNode objectNode = restConnector.callGetRequest(
            STATION_URL, "/api/v1/suggests", params, ObjectNode.class
        );
        List<StationDto> stations = new ArrayList<>();
        fillStations(objectNode, stations, "city", "train");
        return stations.stream()
            .filter(stationDto -> hasText(stationDto.code())) // оставляем только с кодом
            .collect(Collectors.toMap(
                station -> station.name() + ":" + station.code(), // ключ для исключения дублей
                station -> station,
                (firstValue, newValue) -> firstValue // если ключ повторяется, оставляем первый
            ))
            .values().stream()
            .sorted(Comparator.comparing(StationDto::name))
            .toList();
    }

    private static void fillStations(ObjectNode objectNode, List<StationDto> stations, String... fields) {
        if (fields == null) {
            return;
        }
        for (String field : fields) {
            JsonNode jsonNode = objectNode.findPath(field);
            jsonNode.forEach(
                node -> stations.add(
                    StationDtoV2.builder()
                        .name(node.path("name").asText())
                        .expressCode(node.path("expressCode").asText())
                        .region(node.path("region").asText())
                        .suburbanCode(node.path("suburbanCode").asText())
                        .regionIso(node.path("regionIso").asText())
                        .build()
                )
            );
        }
    }
}
