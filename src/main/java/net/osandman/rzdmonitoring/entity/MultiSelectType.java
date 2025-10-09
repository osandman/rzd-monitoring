package net.osandman.rzdmonitoring.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MultiSelectType {
    SEAT_FILTER(2),
    TRAIN_NUMBER(1);

    private final int columnCount;
}
