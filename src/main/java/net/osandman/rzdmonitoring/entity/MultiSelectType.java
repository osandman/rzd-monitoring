package net.osandman.rzdmonitoring.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MultiSelectType {
    SEAT_FILTERS(2),
    ROUTES(1);

    private final int columnCount;
}
