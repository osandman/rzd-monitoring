package net.osandman.rzdmonitoring.dto.station;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Station {
    @JsonProperty("n")
    public String name;
    @JsonProperty("c")
    public String code;
    @JsonProperty("S")
    public int s;
    @JsonProperty("L")
    public int l;
    public boolean ss;
}
