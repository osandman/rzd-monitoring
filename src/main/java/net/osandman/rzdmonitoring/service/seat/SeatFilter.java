package net.osandman.rzdmonitoring.service.seat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.osandman.rzdmonitoring.dto.train.SeatDto;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum SeatFilter {

    // Типы мест (категория: SEAT_TYPE) - работают по ИЛИ
    UP_SEATS(seatDto -> seatDto.seatLabel().toLowerCase().contains("верх"), "Верхние", FilterGroup.SEAT_TYPE),
    DOWN_SEATS(seatDto -> seatDto.seatLabel().toLowerCase().contains("ниж"), "Нижние", FilterGroup.SEAT_TYPE),

    // Типы вагонов (категория: CAR_TYPE) - работают по ИЛИ
    SEATING(seatDto -> seatDto.seatLabel().toLowerCase().contains("сид"), "Сидячий", FilterGroup.CAR_TYPE),
    COMPARTMENT(seatDto -> seatDto.carType().toLowerCase().contains("купе"), "Купе", FilterGroup.CAR_TYPE),
    PLATZKART(seatDto -> seatDto.carType().toLowerCase().contains("плац"), "Плацкарт", FilterGroup.CAR_TYPE),
    SV(seatDto -> seatDto.carType().toLowerCase().contains("св"), "СВ", FilterGroup.CAR_TYPE),

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

    public static List<String> getButtonsForAvailableCarTypes(Set<String> availableCarTypes) {
        if (availableCarTypes == null || availableCarTypes.isEmpty()) {
            return getButtons();
        }

        Set<String> lowerCaseTypes = availableCarTypes.stream()
            .map(String::toLowerCase)
            .collect(Collectors.toSet());

        // Анализируем доступные типы
        boolean hasCoupe = lowerCaseTypes.stream().anyMatch(type -> type.contains("купе"));
        boolean hasPlatz = lowerCaseTypes.stream().anyMatch(type -> type.contains("плац"));
        boolean hasSV = lowerCaseTypes.stream().anyMatch(type -> type.contains("св"));
        boolean hasSeating = lowerCaseTypes.stream().anyMatch(type -> type.contains("сид"));

        boolean hasCouchettes = hasCoupe || hasPlatz || hasSV;  // Есть спальные места
        int carTypeCount = (hasCoupe ? 1 : 0) + (hasPlatz ? 1 : 0) + (hasSV ? 1 : 0) + (hasSeating ? 1 : 0);

        return Arrays.stream(SeatFilter.values())
            .filter(filter -> {
                return switch (filter) {
                    // Типы вагонов - только если есть выбор
                    case COMPARTMENT -> carTypeCount > 1 && hasCoupe;
                    case PLATZKART -> carTypeCount > 1 && hasPlatz;
                    case SV -> carTypeCount > 1 && hasSV;
                    case SEATING -> carTypeCount > 1 && hasSeating;

                    // Позиции мест - только для спальных вагонов
                    case UP_SEATS, DOWN_SEATS -> hasCouchettes;

                    // Боковые места - только для плацкарта
                    case NOT_SIDE -> hasPlatz;

                    // Мужские/женские купе - только для купе/СВ
                    case NOT_FOR_WOMAN, NOT_FOR_MAN -> hasCoupe || hasSV;

                    // Все остальные фильтры показываем всегда
                    default -> true;
                };
            })
            .map(SeatFilter::getButtonText)
            .toList();
    }

    public static boolean checkAllCarTypesExist(Set<String> carTypes) {
        if (carTypes == null || carTypes.isEmpty()) {
            return true;
        }
        Set<String> allCarTypes = Set.of("СИД", "ПЛАЦ", "КУПЕ", "СВ");
        return carTypes.containsAll(allCarTypes);
    }

    @Override
    public String toString() {
        return getButtonText();
    }
}
