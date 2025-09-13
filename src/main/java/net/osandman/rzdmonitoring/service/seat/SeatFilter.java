package net.osandman.rzdmonitoring.service.seat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.osandman.rzdmonitoring.dto.SeatDto;

import java.util.function.Predicate;

@Getter
@AllArgsConstructor
public enum SeatFilter {

    ALL_SEATS(seatDto -> true, "Любое"),
    UP_SEATS(seatDto -> seatDto.seatLabel().toLowerCase().contains("верх"), "Верхние"),
    DOWN_SEATS(seatDto -> seatDto.seatLabel().toLowerCase().contains("ниж"), "Нижние"),
    FOR_INVALID(seatDto -> seatDto.seatLabel().toLowerCase().contains("инвалид"), "Для инвалидов"),
    NOT_INVALID(seatDto -> !seatDto.seatLabel().toLowerCase().contains("инвалид"), "Кроме инвалидов"),
    SEATING_SEATS(seatDto -> seatDto.seatLabel().toLowerCase().contains("сид"), "Сидячие"),
    COMPARTMENT(seatDto -> seatDto.carType().toLowerCase().contains("купе"), "Купе"),
    PLATZKART(seatDto -> seatDto.carType().toLowerCase().contains("плац"), "Плацкарт"),
    NOT_WITH_CHILDREN(seatDto -> !seatDto.seatLabel().toLowerCase().contains("детьми"), "Не с детьми"),
    NOT_LAST(seatDto -> !seatDto.seatLabel().toLowerCase().contains("последнее"), "Не последнее купе"),
    NOT_FOR_WOMAN(seatDto -> !seatDto.seatLabel().toLowerCase().contains("женское"), "Не женское купе"),
    NOT_FOR_MAN(seatDto -> !seatDto.seatLabel().toLowerCase().contains("мужское"), "Не мужское купе"),
    ;

    private final Predicate<SeatDto> predicate;
    private final String buttonText;

    public static SeatFilter getByButtonText(String buttonText) {
        for (SeatFilter filter : values()) {
            if (filter.buttonText.equals(buttonText)) {
                return filter;
            }
        }
        throw new IllegalArgumentException("No such filter: '%s'".formatted(buttonText));
    }
}
