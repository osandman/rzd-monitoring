package net.osandman.rzdmonitoring.bot.command;

public enum ParamEnum {
    FROM_STATION_CODE("from_station_code"),
    FROM_STATION("from_station"),
    TO_STATION_CODE("to_station_code"),
    TO_STATION("to_station"),
    DATE("date"),
    TRAIN_NUMBER("train_number");

    final String value;

    ParamEnum(String value) {
        this.value = value;
    }
}