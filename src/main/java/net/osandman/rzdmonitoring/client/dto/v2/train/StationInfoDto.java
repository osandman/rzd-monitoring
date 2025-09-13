package net.osandman.rzdmonitoring.client.dto.v2.train;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class StationInfoDto {
    @JsonProperty("StationName")
    private String stationName;

    @JsonProperty("StationCode")
    private String stationCode;

    @JsonProperty("CnsiCode")
    private String cnsiCode;

    @JsonProperty("RegionName")
    private String regionName;

    @JsonProperty("IsoCode")
    private String isoCode;
}
