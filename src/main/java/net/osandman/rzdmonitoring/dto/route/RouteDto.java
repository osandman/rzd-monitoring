package net.osandman.rzdmonitoring.dto.route;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RouteDto {
    private String trainNumber;
    private String trainName;
    private String fromStation;
    private String toStation;
    private String fromStationCode;
    private String toStationCode;
    private LocalDateTime departureDateTime;
    private LocalDateTime arrivalDateTime;
    private Boolean isSuburban;
    private String carrier;
    private List<CarriageDto> carriages;
}
