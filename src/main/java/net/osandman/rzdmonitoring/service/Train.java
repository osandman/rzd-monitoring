package net.osandman.rzdmonitoring.service;

public enum Train {
    PASSENGER_TRAIN("1"),
    ELECTRIC_TRAIN("2"),
    ALL("3");

    public final String value;

    Train(String value) {
        this.value = value;
    }
}
