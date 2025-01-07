package net.osandman.rzdmonitoring.service.station;

import net.osandman.rzdmonitoring.dto.station.StationDto;

import java.util.List;

public interface StationService {

    List<StationDto> findStations(String partName);
}
