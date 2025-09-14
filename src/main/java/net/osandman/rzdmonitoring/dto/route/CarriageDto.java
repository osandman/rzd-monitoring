package net.osandman.rzdmonitoring.dto.route;

import lombok.Data;

@Data
public class CarriageDto {
    private String type;
    private String typeName;
    private String serviceClass;
    private int lowerSeats;
    private int upperSeats;
    private int lowerSideSeats;
    private int upperSideSeats;
    private int totalSeats;
}
