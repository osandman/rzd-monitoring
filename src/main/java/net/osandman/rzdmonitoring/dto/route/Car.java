package net.osandman.rzdmonitoring.dto.route;

import lombok.Data;

@Data
public class Car {
    public int carDataType;
    public int itype;
    public String type;
    public String typeLoc;
    public int freeSeats;
    public int pt;
    public int tariff;
    public String servCls;
    public boolean disabledPerson;
    public boolean lastPlaces;
}
