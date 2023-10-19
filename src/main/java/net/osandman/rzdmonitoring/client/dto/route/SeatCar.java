package net.osandman.rzdmonitoring.client.dto.route;

import lombok.Data;

@Data
public class SeatCar {
    public int carDataType;
    public String servCls;
    public String tariff;
    public String tariff2;
    public int itype;
    public String type;
    public String typeLoc;
    public int freeSeats;
    public boolean disabledPerson;
}
