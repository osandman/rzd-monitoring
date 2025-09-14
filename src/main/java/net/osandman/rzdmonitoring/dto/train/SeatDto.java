package net.osandman.rzdmonitoring.dto.train;

import org.springframework.lang.NonNull;

public record SeatDto(
    String carNumber,
    String carType,
    String seatLabel,
    String seatPlaces,
    String seatTariff,
    Integer seatFree
) {

    @Override
    @NonNull
    public String toString() {
        return "вагон " + carNumber + "(" + carType.toLowerCase() + "), "
               + "свободных мест " + seatFree + "(" + seatLabel.toLowerCase() + "), " + seatTariff + "р";
    }
}
