package net.osandman.rzdmonitoring.dto.route;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CarType {
    public String type;
    @JsonProperty("itype")
    public int iType;
}
