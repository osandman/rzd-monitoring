package net.osandman.rzdmonitoring.dto.route;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;

import java.util.List;

@JsonTypeName("List")
@Data
public class Route {
    public String number;
    public String number2;
    public int type;
    public int typeEx;
    public int subt;
    public String subtrainCatName;
    public int depth;
    @JsonProperty("new")
    public boolean myNew;
    public boolean elReg;
    public boolean deferredPayment;
    public boolean varPrice;
    public int code0;
    public int code1;
    public boolean bEntire;
    public String trainName;
    public boolean brandLogo;
    public String brand;
    public int brandId;
    public String carrier;
    public String route0;
    public String route1;
    public int routeCode0;
    public int routeCode1;
    public String trDate0;
    public String trTime0;
    public String station0;
    public String station1;
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
    public String timeInWay;
    public int flMsk;
    public String stList;
    public String stListX;
    public String mvMode;
    public boolean chWarn;
    public boolean relev;
    public boolean onWay;
    public String suburbanTrainName;
    public int subTabloVisible;
    public int train_id;
    public List<Car> cars;
    public List<SeatCar> seatCars;
    public String test;
    public int testSize;
    public List<CarType> carTypes;
    public boolean disabledType;
    public boolean nonRefundable;
    public boolean addFood;
    public boolean addGoods;
    public int addCompLuggageNum;
    public boolean addCompLuggage;
    public boolean addHandLuggage;
    public boolean bFirm;
}
