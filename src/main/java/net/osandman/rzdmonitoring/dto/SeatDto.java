package net.osandman.rzdmonitoring.dto;

public record SeatDto(
    String carNumber,
    String carType,
    String seatLabel,
    String seatPlaces,
    String seatTariff,
    int seatFree
) {

    @Override
    public String toString() {
        return "вагон: " + carNumber + "(" + carType.toLowerCase() + "), "
               + "свободных мест - " + seatFree + "(" + seatLabel.toLowerCase() + "), " + seatTariff + "р;";
    }
}
