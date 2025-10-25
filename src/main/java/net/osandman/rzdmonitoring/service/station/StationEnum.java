package net.osandman.rzdmonitoring.service.station;

public enum StationEnum {
    PERM_2("2030400"),
    PERM_ALL("2030120"),
    MOSCOW_YAR("2000002"),
    MOSCOW_ALL("2000000"),
    LYSVA("2030104"),
    CHUSOVSKAYA("2030070"),
    KRASNODAR("2064788");

    private final String code;

    StationEnum(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return this.name();
    }

    public String code() {
        return code;
    }
}
