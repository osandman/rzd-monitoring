package net.osandman.rzdmonitoring.service;

public enum Direction {
    ONE_WAY("0"),
    ROUND_TRIP("1");

    public final String value;
    Direction(String value) {
        this.value = value;
    }
}
