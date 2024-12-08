package net.osandman.rzdmonitoring.dto;

public record StationDto(String name, String code, int state, int level) {
    @Override
    public String toString() {
        return name;
    }
}
