package net.osandman.rzdmonitoring.api;

import lombok.RequiredArgsConstructor;
import net.osandman.rzdmonitoring.dto.station.StationDto;
import net.osandman.rzdmonitoring.service.station.StationServiceV2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v2/rzd")
@RequiredArgsConstructor
public class RzdControllerV2 {

    private final StationServiceV2 stationService;

    @GetMapping("/station")
    private List<StationDto> findStation(@RequestParam String name) {
        return stationService.findStations(name);
    }
}
