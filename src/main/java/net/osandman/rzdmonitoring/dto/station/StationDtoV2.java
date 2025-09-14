package net.osandman.rzdmonitoring.dto.station;

import lombok.Builder;

@Builder
public record StationDtoV2(
    String name, String expressCode, String region, String suburbanCode, String regionIso
) implements StationDto {

    @Override
    public String toString() {
        return name;
    }

    @Override
    public String region() {
        return region.replace("Российская Федерация", "РФ");
    }

    @Override
    public String code() {
        return expressCode;
    }

    @Override
    public String printStr() {
        return "%s код=%s%s; регион=%s; iso=%s"
            .formatted(
                name(),
                code(),
                suburbanCode() != null ? "(пригород. код=" + suburbanCode() + ")" : "",
                region(),
                regionIso()
            );
    }
}
