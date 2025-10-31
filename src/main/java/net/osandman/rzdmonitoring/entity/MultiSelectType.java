package net.osandman.rzdmonitoring.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MultiSelectType {
    SEAT_FILTERS(2, "фильтры поиска"),
    ROUTES(1, "маршруты");

    private final int columnCount;
    private final String description;
}
