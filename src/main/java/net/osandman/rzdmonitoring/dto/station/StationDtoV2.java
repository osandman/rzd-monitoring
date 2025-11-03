package net.osandman.rzdmonitoring.dto.station;

import lombok.Builder;

import static org.springframework.util.StringUtils.hasText;

@Builder
public record StationDtoV2(
    String name,
    String expressCode,
    String foreignCode,
    String region,
    String suburbanCode,
    String regionIso,
    String countryIso
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
        return hasText(expressCode) ? expressCode : hasText(foreignCode) ? foreignCode + "(иностр.)" : "";
    }

    @Override
    public String printStr() {
        return "%s, регион: %s".formatted(name(), region());
    }

//    @Override
//    public String printStr() {
//        return "%s код=%s%s; регион=%s%s%s"
//            .formatted(
//                name(),
//                code(),
//                hasText(suburbanCode) ? " (пригород.код=" + suburbanCode + ")" : "",
//                region(),
//                hasText(regionIso) ? "; region iso=" + regionIso : "",
//                hasText(countryIso) ? "; country iso=" + countryIso : ""
//            );
//    }
}
