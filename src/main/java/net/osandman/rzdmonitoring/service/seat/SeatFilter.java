package net.osandman.rzdmonitoring.service.seat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.osandman.rzdmonitoring.dto.train.SeatDto;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

@Getter
@AllArgsConstructor
public enum SeatFilter {

    UP_SEATS(seatDto -> seatDto.seatLabel().toLowerCase().contains("верх"), "Верхние"),
    DOWN_SEATS(seatDto -> seatDto.seatLabel().toLowerCase().contains("ниж"), "Нижние"),
    FOR_INVALID(seatDto -> seatDto.seatLabel().toLowerCase().contains("инвалид"), "Для инвалидов"),
    NOT_INVALID(seatDto -> !seatDto.seatLabel().toLowerCase().contains("инвалид"), "Кроме инвалидов"),
    COMPARTMENT(seatDto -> seatDto.carType().toLowerCase().contains("купе"), "Купе"),
    PLATZKART(seatDto -> seatDto.carType().toLowerCase().contains("плац"), "Плацкарт"),
    SEATING_SEATS(seatDto -> seatDto.seatLabel().toLowerCase().contains("сид"), "Сидячие"),
    NOT_WITH_CHILDREN(seatDto -> !seatDto.seatLabel().toLowerCase().contains("детьми"), "Без детей"),
    NOT_FOR_WOMAN(seatDto ->
        seatDto.seatLabel().toLowerCase().contains("женское")
        && !seatDto.seatLabel().toLowerCase().contains("мужское"),
        "Женское купе"),
    NOT_FOR_MAN(seatDto ->
        seatDto.seatLabel().toLowerCase().contains("мужское")
        && !seatDto.seatLabel().toLowerCase().contains("женское"),
        "Мужское купе"),
    NOT_SIDE(seatDto -> !seatDto.seatLabel().toLowerCase().contains("боковое"), "Не боковое"),
    NOT_LAST(seatDto -> !seatDto.seatLabel().toLowerCase().contains("последнее"), "Не последнее купе"),
    ALL_SEATS(seatDto -> true, "Любое"),
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

    public static List<String> getButtons() {
        return Arrays.stream(SeatFilter.values())
            .map(SeatFilter::getButtonText)
            .toList();
    }

    @Override
    public String toString() {
        return getButtonText();
    }
}
