package net.osandman.rzdmonitoring.service.station;

import lombok.RequiredArgsConstructor;
import net.osandman.rzdmonitoring.client.RestConnector;
import net.osandman.rzdmonitoring.client.dto.station.Station;
import net.osandman.rzdmonitoring.dto.station.StationDto;
import net.osandman.rzdmonitoring.dto.station.StationDtoImpl;
import net.osandman.rzdmonitoring.util.JsonParser;
import net.osandman.rzdmonitoring.util.Utils;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StationServiceImpl implements StationService {

    public static final String STATION_ENDPOINT = "/suggester";

    private final RestConnector restConnector;

    @Override
    public List<StationDto> findStations(String partName) { // часть имени
        List<StationDto> stationDtos = new ArrayList<>();
        getStations(partName).stream()
            .filter(station -> station.getName().toLowerCase().contains(partName.toLowerCase()))
            // && station.getS() >= 4
            .forEach(
                station -> stationDtos.add(
                    new StationDtoImpl(station.getName(), station.getCode(), station.getS(), station.getL())
                ));
        return stationDtos;
    }

    private List<Station> getStations(String partName) {
        String lang = Utils.detectLang(partName);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>() {{
            put("stationNamePart", List.of(partName.toUpperCase()));
            put("lang", List.of(lang));
        }};
        String stationsStr = restConnector.callGetRequest(STATION_ENDPOINT, params);
        return JsonParser.parseValues(stationsStr, Station.class);
    }
}
