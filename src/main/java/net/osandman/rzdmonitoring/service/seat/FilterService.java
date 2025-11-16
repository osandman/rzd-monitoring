package net.osandman.rzdmonitoring.service.seat;

import net.osandman.rzdmonitoring.dto.train.SeatDto;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class FilterService {

    public List<SeatDto> filterSeats(List<SeatDto> seats, Set<SeatFilter> seatFilters) {
        if (seatFilters.isEmpty()) {
            return seats;
        }

        // Группируем фильтры по категориям
        Map<SeatFilter.FilterGroup, List<SeatFilter>> groupedFilters = seatFilters.stream()
            .collect(Collectors.groupingBy(SeatFilter::getGroup));

        // Проверяем есть ли гендерные фильтры
        boolean hasGenderFilters = groupedFilters.containsKey(SeatFilter.FilterGroup.GENDER);

        return seats.stream()
            .filter(seatDto -> passesAllFilters(seatDto, groupedFilters))
            .map(seatDto -> {
                // Если есть гендерные фильтры - фильтруем места внутри seatPlaces
                if (hasGenderFilters && seatDto.seatPlaces() != null && !seatDto.seatPlaces().isEmpty()) {
                    return filterSeatPlacesByGender(seatDto, groupedFilters.get(SeatFilter.FilterGroup.GENDER));
                }
                return seatDto;
            })
            .filter(Objects::nonNull) // Убираем null (если не осталось подходящих мест)
            .sorted(Comparator.comparing(SeatDto::carNumber).thenComparing(SeatDto::seatLabel))
            .toList();
    }

    private boolean passesAllFilters(SeatDto seatDto, Map<SeatFilter.FilterGroup, List<SeatFilter>> groupedFilters) {
        for (Map.Entry<SeatFilter.FilterGroup, List<SeatFilter>> entry : groupedFilters.entrySet()) {
            SeatFilter.FilterGroup group = entry.getKey();
            List<SeatFilter> filters = entry.getValue();

            boolean passesGroupFilter = switch (group) {
                case CAR_TYPE, SEAT_TYPE, GENDER ->
                    filters.stream().anyMatch(filter -> filter.getPredicate().test(seatDto));
                case SPECIAL, POSITION, UNIVERSAL ->
                    filters.stream().allMatch(filter -> filter.getPredicate().test(seatDto));
            };

            if (!passesGroupFilter) {
                return false;
            }
        }
        return true;
    }

    private SeatDto filterSeatPlacesByGender(SeatDto originalSeat, List<SeatFilter> genderFilters) {
        String seatPlaces = originalSeat.seatPlaces();

        if (seatPlaces == null || seatPlaces.trim().isEmpty()) {
            return originalSeat; // Нет данных о местах - возвращаем как есть
        }

        // Разбиваем строку на отдельные места
        List<String> allPlaces = Arrays.stream(seatPlaces.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList();

        if (allPlaces.isEmpty()) {
            return originalSeat;
        }

        // Определяем какой пол нужен
        boolean needWomen = genderFilters.stream().anyMatch(f -> f == SeatFilter.FOR_WOMAN);
        boolean needMen = genderFilters.stream().anyMatch(f -> f == SeatFilter.FOR_MAN);

        // Фильтруем места по полу
        List<String> filteredPlaces = allPlaces.stream()
            .filter(place -> matchesGenderFilter(place, needWomen, needMen))
            .toList();

        // Если не осталось подходящих мест - возвращаем null
        if (filteredPlaces.isEmpty()) {
            return null;
        }

        // Создаем новый SeatDto с обновленными данными
        String newSeatPlaces = String.join(", ", filteredPlaces);
        int newSeatFree = filteredPlaces.size();

        return SeatDto.builder()
            .carNumber(originalSeat.carNumber())
            .carType(originalSeat.carType())
            .seatLabel(originalSeat.seatLabel() + ": " + newSeatPlaces)
            .seatPlaces(newSeatPlaces)
            .seatTariff(originalSeat.seatTariff())
            .seatFree(newSeatFree)
            .build();
    }

    /**
     * Проверяет соответствует ли место гендерному фильтру
     */
    private boolean matchesGenderFilter(String place, boolean needWomen, boolean needMen) {
        String upperPlace = place.toUpperCase();

        // Женские места (содержат "Ж")
        if (needWomen && upperPlace.contains("Ж")) {
            return true;
        }

        // Мужские места (содержат "М")
        if (needMen && upperPlace.contains("М")) {
            return true;
        }

        // Смешанные места (содержат "С") - показываем только если не выбран конкретный пол
        if (!needWomen && !needMen && upperPlace.contains("С")) {
            return true;
        }

        return false;
    }
}