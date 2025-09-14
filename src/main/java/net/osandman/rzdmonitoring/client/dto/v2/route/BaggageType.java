package net.osandman.rzdmonitoring.client.dto.v2.route;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BaggageType {

    @JsonProperty("Type")
    private String type;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("CarBaggageInfo")
    private Object carBaggageInfo;
}
