package net.osandman.rzdmonitoring.client.dto.route;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class Tp {
    public String from;
    public int fromCode;
    public String where;
    public int whereCode;
    public String date;
    public boolean noSeats;
    public String defShowTime;
    public String state;
    public List<Route> list;
    public List<Map<String, String>> msgList;
}
