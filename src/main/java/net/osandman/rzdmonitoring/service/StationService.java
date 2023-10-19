package net.osandman.rzdmonitoring.service;

import net.osandman.rzdmonitoring.client.RequestProcess;
import net.osandman.rzdmonitoring.dto.StationDto;
import net.osandman.rzdmonitoring.client.dto.station.Station;
import net.osandman.rzdmonitoring.mapping.MapperImpl;
import net.osandman.rzdmonitoring.util.JsonParser;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Comparator;
import java.util.List;

@Service
public class StationService extends BaseService {
    public static final String STATION_ENDPOINT = "/suggester";

    public StationService(RequestProcess requestProcess, MapperImpl printer) {
        super(STATION_ENDPOINT, requestProcess, printer);
    }

    public StationDto findStation(String namePart) {
        List<Station> stations = getStations(namePart);
        return findStationDto(namePart, stations);
    }

    @NonNull
    public List<StationDto> findStations(String namePart) {
        return getStations(namePart).stream()
                .filter(station -> station.getName().toLowerCase().contains(namePart.toLowerCase()) &&
                        station.getS() >= 4)
                .map(station -> new StationDto(station.getName(), station.getCode()))
                .toList();
    }


    private List<Station> getStations(String namePart) {
        String lang = getLang(namePart);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>() {{
            put("stationNamePart", List.of(namePart.toUpperCase()));
            put("lang", List.of(lang));
        }};
        String stationsStr = requestProcess.callGetRequest(STATION_ENDPOINT, params);
        return JsonParser.parseValues(stationsStr, Station.class);
    }

    private static StationDto findStationDto(String namePart, List<Station> stations) {
        final String[] stationCode = new String[1];
        String findedName = stations.stream()
                .sorted(Comparator.comparing(Station::getName))
                .filter(station -> {
                    boolean exist = station.getName().toLowerCase().contains(namePart.toLowerCase()) &&
                            station.getS() >= 4;
                    if (exist) {
                        stationCode[0] = station.getCode();
                    }
                    return exist;
                })
                .findFirst().map(Station::getName).orElse(null);
        return new StationDto(findedName, stationCode[0]);
    }

    private static String getLang(String namePart) {
        int ind = 0;
        char checkingChar = namePart.charAt(ind++);
        while (!Character.isLetter(checkingChar)) {
            checkingChar = namePart.charAt(ind++);
        }
        return Character.UnicodeScript.of(checkingChar) == Character.UnicodeScript.CYRILLIC ?
                "ru" : "en";
    }
}
