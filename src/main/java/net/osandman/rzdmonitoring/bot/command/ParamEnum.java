package net.osandman.rzdmonitoring.bot.command;

public enum ParamEnum {
    FROM_STATION("from_station"),
    TO_STATION("to_station"),
    DATE("date"),
    TRAIN_NUMBER("train_number");
    final String value;

    ParamEnum(String value) {
        this.value = value;
    }
}