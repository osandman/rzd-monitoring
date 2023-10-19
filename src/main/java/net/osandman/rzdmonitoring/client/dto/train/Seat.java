package net.osandman.rzdmonitoring.client.dto.train;

import lombok.Data;

@Data
public class Seat {
    public String type;
    public String code;
    public String label;
    public String tariff;
    public String tariff2;
    public String tariffServ;
    public int free;
    public String placesNonRef;
    public int freeRef;
    public String placesRef;
    public String places;
    public String tariffNonRef;
    public String tariff2NonRef;
    public int freeNonRef;
}
