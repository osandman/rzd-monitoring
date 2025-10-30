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

    // Типы мест (категория: SEAT_TYPE) - работают по ИЛИ
    UP_SEATS(seatDto -> seatDto.seatLabel().toLowerCase().contains("верх"), "Верхние", FilterGroup.SEAT_TYPE),
    DOWN_SEATS(seatDto -> seatDto.seatLabel().toLowerCase().contains("ниж"), "Нижние", FilterGroup.SEAT_TYPE),
    SEATING_SEATS(seatDto -> seatDto.seatLabel().toLowerCase().contains("сид"), "Сидячие", FilterGroup.SEAT_TYPE),

    // Типы вагонов (категория: CAR_TYPE) - работают по ИЛИ
    COMPARTMENT(seatDto -> seatDto.carType().toLowerCase().contains("купе"), "Купе", FilterGroup.CAR_TYPE),
    PLATZKART(seatDto -> seatDto.carType().toLowerCase().contains("плац"), "Плацкарт", FilterGroup.CAR_TYPE),

    // Специальные требования (категория: SPECIAL) - работают по И
    FOR_INVALID(seatDto -> seatDto.seatLabel().toLowerCase().contains("инвалид"), "Для инвалидов", FilterGroup.SPECIAL),
    NOT_INVALID(seatDto -> !seatDto.seatLabel().toLowerCase().contains("инвалид"), "Кроме инвалидов", FilterGroup.SPECIAL),
    NOT_WITH_CHILDREN(seatDto -> !seatDto.seatLabel().toLowerCase().contains("детьми"), "Без детей", FilterGroup.SPECIAL),

    // Пол (категория: GENDER) - работают по ИЛИ
    NOT_FOR_WOMAN(seatDto -> seatDto.seatLabel().toLowerCase().contains("женское")
                             && !seatDto.seatLabel().toLowerCase().contains("мужское"), "Женское купе", FilterGroup.GENDER),
    NOT_FOR_MAN(seatDto -> seatDto.seatLabel().toLowerCase().contains("мужское")
                           && !seatDto.seatLabel().toLowerCase().contains("женское"), "Мужское купе", FilterGroup.GENDER),

    // Позиция (категория: POSITION) - работают по И
    NOT_SIDE(seatDto -> !seatDto.seatLabel().toLowerCase().contains("боковое"), "Не боковое", FilterGroup.POSITION),
    NOT_LAST(seatDto -> !seatDto.seatLabel().toLowerCase().contains("последнее"), "Не последнее купе", FilterGroup.POSITION),

    // Универсальный
    ALL_SEATS(seatDto -> true, "Любое", FilterGroup.UNIVERSAL);

    private final Predicate<SeatDto> predicate;
    private final String buttonText;
    private final FilterGroup group;

    // Enum для группировки фильтров
    public enum FilterGroup {
        SEAT_TYPE,    // Типы мест - ИЛИ
        CAR_TYPE,     // Типы вагонов - ИЛИ
        SPECIAL,      // Специальные требования - И
        GENDER,       // Пол - ИЛИ
        POSITION,     // Позиция - И
        UNIVERSAL     // Универсальные - И
    }

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
