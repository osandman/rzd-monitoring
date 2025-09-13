package net.osandman.rzdmonitoring.client.dto.v2.train;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ServicesWithIndicationDto {
    @JsonProperty("CarService")
    private String carService;

    @JsonProperty("Indication")
    private String indication;
}
