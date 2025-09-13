package net.osandman.rzdmonitoring.client.dto.v2.train;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FreePlacesByCompartmentsDto {
    @JsonProperty("CompartmentNumber")
    private String compartmentNumber;

    @JsonProperty("Places")
    private String places;
}
