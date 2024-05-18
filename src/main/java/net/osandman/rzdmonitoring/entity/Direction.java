package net.osandman.rzdmonitoring.entity;

/**
 * Коды параметра типа поездки.
 */
public enum Direction {
    ONE_WAY("0"), // в одну сторону
    ROUND_TRIP("1"); // туда-обратно

    public final String code;

    Direction(String code) {
        this.code = code;
    }
}
