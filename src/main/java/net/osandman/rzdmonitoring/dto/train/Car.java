package net.osandman.rzdmonitoring.dto.train;

import lombok.Data;

import java.util.List;

@Data
public class Car {
    public String cnumber;
    public String type;
    public String catLabelLoc;
    public String typeLoc;
    public String catCode;
    public int ctypei;
    public int ctype;
    public String letter;
    public String clsType;
    public String subType;
    public String clsName;
    public List<Service> services;
    public String tariff;
    public String tariff2;
    public Object tariffServ;
    public String addSigns;
    public String carrier;
    public int carrierId;
    public boolean insuranceFlag;
    public int insuranceTypeId;
    public String owner;
    public boolean elReg;
    public boolean food;
    public boolean selFood;
    public boolean equippedSIOP;
    public boolean addFood;
    public boolean regularFoodService;
    public boolean noSmok;
    public boolean inetSaleOff;
    public boolean bVip;
    public boolean conferenceRoomFlag;
    public boolean bDeck2;
    public Object intServiceClass;
    public Object specialSeatTypes;
    public boolean deferredPayment;
    public boolean varPrice;
    public boolean ferry;
    public int seniorTariff;
    public boolean bedding;
    public boolean nonRefundable;
    public boolean addTour;
    public boolean addGoods;
    public boolean addHandLuggage;
    public boolean youth;
    public boolean unior;
    public List<Seat> seats;
    public String places;
    public int schemeId;
    public boolean forcedBedding;
    public boolean policyEnabled;
    public boolean msr;
    public boolean medic;
    public SchemeInfo schemeInfo;
}
