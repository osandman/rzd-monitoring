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

    public static final String BASE_URL = "https://ticket.rzd.ru";

    private final RestConnector restConnector;

    @Override
    public List<StationDto> findStations(String partName) {
        String lang = Utils.detectLang(partName);
        String strToFind = Utils.removeBracketsWithContent(partName);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>() {{
            put("GroupResults", List.of("true"));
            put("Query", List.of(strToFind));
            put("Language", List.of(lang));
        }};
        ObjectNode objectNode = restConnector.callGetRequest(
            BASE_URL, "/api/v1/suggests", params, ObjectNode.class
        );
        List<StationDto> stations = fillStations(objectNode, "city", "train");
        return stations.stream()
            .filter(stationDto -> hasText(stationDto.code())) // оставляем только с кодом
            .collect(Collectors.toMap(
                StationDto::code, // ключ для исключения дублей
                station -> station,
                (firstValue, newValue) -> firstValue // если ключ повторяется, оставляем первый
            ))
            .values().stream()
            .sorted(Comparator.comparing(StationDto::name))
            .toList();
    }

    private static List<StationDto> fillStations(ObjectNode objectNode, String... fields) {
        List<StationDto> stations = new ArrayList<>();
        if (fields == null) {
            return stations;
        }
        for (String field : fields) {
            JsonNode jsonNode = objectNode.findPath(field);
            jsonNode.forEach(node -> {
                String name = node.path("name").asText();
                String expressCode = node.path("expressCode").asText();
                String foreignCode = node.path("foreignCode").asText();
                String region = node.path("region").asText();
                String suburbanCode = node.path("suburbanCode").asText();
                String regionIso = node.path("regionIso").asText();

                // Проверка: есть ли уже станция с таким name
                String finalName = name;
                boolean nameExists = stations.stream()
                    .anyMatch(st -> st.name().equals(finalName));
                // если есть дубль, то добавляем в скобках код к имени, чтобы различать
                if (nameExists) {
                    name = name + " (" + (hasText(expressCode) ? expressCode : foreignCode) + ")";
                }

                stations.add(
                    StationDtoV2.builder()
                        .name(name)
                        .expressCode(expressCode)
                        .foreignCode(foreignCode)
                        .region(region)
                        .suburbanCode(suburbanCode)
                        .regionIso(regionIso)
                        .build()
                );
            });
        }
        return stations;
    }
}
