package net.osandman.rzdmonitoring.bot.command;

public enum ParamType {
    FROM_STATION_CODE("from_station_code"),
    FROM_STATION("from_station"),
    TO_STATION_CODE("to_station_code"),
    TO_STATION("to_station"),
    DATE("date"),
    TRAIN_NUMBERS("train_numbers"),
    SEAT_FILTERS("seat_filters"),
    ;

    final String value;

    ParamType(String value) {
        this.value = value;
    }
}