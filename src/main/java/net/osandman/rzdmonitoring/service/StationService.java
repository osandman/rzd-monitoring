package net.osandman.rzdmonitoring.service;

import net.osandman.rzdmonitoring.client.RequestProcess;
import net.osandman.rzdmonitoring.client.dto.station.Station;
import net.osandman.rzdmonitoring.dto.StationDto;
import net.osandman.rzdmonitoring.util.JsonParser;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class StationService extends BaseService {
    public static final String STATION_ENDPOINT = "/suggester";

    public StationService(RequestProcess requestProcess) {
        super(STATION_ENDPOINT, requestProcess);
    }

    @NonNull
    public List<StationDto> findStations(String namePart) {
        return getStations(namePart).stream()
                .filter(station -> station.getName().toLowerCase().contains(namePart.toLowerCase())
//                        && station.getS() >= 4
                )
                .map(station -> new StationDto(station.getName(), station.getCode()))
                .toList();
    }


    private List<Station> getStations(String namePart) {
        String lang = detectLang(namePart);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>() {{
            put("stationNamePart", List.of(namePart.toUpperCase()));
            put("lang", List.of(lang));
        }};
        String stationsStr = requestProcess.callGetRequest(STATION_ENDPOINT, params);
        return JsonParser.parseValues(stationsStr, Station.class);
    }

    private static String detectLang(String text) {
        return Pattern.matches(".*\\p{InCYRILLIC}.*", text) ? "ru" : "en";
    }
}
