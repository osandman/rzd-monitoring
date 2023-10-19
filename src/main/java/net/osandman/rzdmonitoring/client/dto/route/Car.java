package net.osandman.rzdmonitoring.client.dto.route;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Car {
    public int carDataType;
    @JsonProperty("itype")
    public int iType;
    public String type;
    public String typeLoc;
    public int freeSeats;
    public int pt;
    public int tariff;
    public String servCls;
    public boolean disabledPerson;
    public boolean lastPlaces;
}
