package net.osandman.rzdmonitoring.dto;

public record StationDto(String name, String code) {
    @Override
    public String toString() {
        return name;
    }
}
