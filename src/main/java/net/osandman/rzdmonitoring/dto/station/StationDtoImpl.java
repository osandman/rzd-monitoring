package net.osandman.rzdmonitoring.dto.station;

public record StationDtoImpl(String name, String code, int state, int level) implements StationDto {

    @Override
    public String toString() {
        return name;
    }

    @Override
    public String printStr() {
        return "код=%s; статус=%d; уровень=%d".formatted(code(), state(), level());
    }
}
