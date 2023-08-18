package net.osandman.rzdmonitoring.dto.train;

import lombok.Data;

import java.util.List;

@Data
public class Lst {
    public String result;
    public String number;
    public String number2;
    public String defShowTime;
    public String date0;
    public String time0;
    public String localDate0;
    public String localTime0;
    public String timeDeltaString0;
    public String date1;
    public String time1;
    public String localDate1;
    public String localTime1;
    public String timeDeltaString1;
    public String type;
    public boolean virtual;
    public boolean bus;
    public boolean boat;
    public String station0;
    public String code0;
    public String station1;
    public String code1;
    public String timeSt0;
    public String timeSt1;
    public String route0;
    public String route1;
    public List<Car> cars;
    public boolean addCompLuggage;
    public List<FunctionBlock> functionBlocks;
    public String timestamp;
}
