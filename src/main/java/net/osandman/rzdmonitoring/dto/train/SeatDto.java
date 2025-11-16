package net.osandman.rzdmonitoring.dto.train;

import lombok.Builder;

@Builder
public record SeatDto(
    String carNumber,
    String carType,
    String seatLabel,
    String seatPlaces,
    String seatTariff,
    Integer seatFree
) {

    public String toPrettyString() {
        return "вагон " + carNumber + " (" + carType.toLowerCase() + ")"
               + " - " + seatFree + " (" + seatLabel.toLowerCase() + ") " + seatTariff + "р";
    }
}
