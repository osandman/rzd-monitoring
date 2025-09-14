package net.osandman.rzdmonitoring.client.dto.v2.route;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Train {

    @JsonProperty("CarGroups")
    private List<CarGroup> carGroups;

    @JsonProperty("IsFromSchedule")
    private Boolean isFromSchedule;

    @JsonProperty("IsTourPackagePossible")
    private Boolean isTourPackagePossible;

    @JsonProperty("CarTransportationsFreePlacesCount")
    private Object carTransportationsFreePlacesCount;

    @JsonProperty("ActualMovement")
    private Object actualMovement;

    @JsonProperty("CategoryId")
    private Integer categoryId;

    @JsonProperty("ScheduleId")
    private Integer scheduleId;

    @JsonProperty("BaggageCarsThreads")
    private Object baggageCarsThreads;

    @JsonProperty("CarTransportationCoachesThreads")
    private Object carTransportationCoachesThreads;

    @JsonProperty("Provider")
    private String provider;

    @JsonProperty("IsWaitListAvailable")
    private Boolean isWaitListAvailable;

    @JsonProperty("BaggageTotalPlaceQuantity")
    private Integer baggageTotalPlaceQuantity;

    @JsonProperty("HasElectronicRegistration")
    private Boolean hasElectronicRegistration;

    @JsonProperty("HasCarTransportationCoaches")
    private Boolean hasCarTransportationCoaches;

    @JsonProperty("HasDynamicPricingCars")
    private Boolean hasDynamicPricingCars;

    @JsonProperty("HasTwoStoreyCars")
    private Boolean hasTwoStoreyCars;

    @JsonProperty("HasSpecialSaleMode")
    private Boolean hasSpecialSaleMode;

    @JsonProperty("Carriers")
    private List<String> carriers;

    @JsonProperty("CarrierDisplayNames")
    private List<String> carrierDisplayNames;

    @JsonProperty("Id")
    private Integer id;

    @JsonProperty("IsBranded")
    private Boolean isBranded;

    @JsonProperty("TrainNumber")
    private String trainNumber;

    @JsonProperty("TrainNumberToGetRoute")
    private String trainNumberToGetRoute;

    @JsonProperty("DisplayTrainNumber")
    private String displayTrainNumber;

    @JsonProperty("TrainDescription")
    private String trainDescription;

    @JsonProperty("TrainName")
    private String trainName;

    @JsonProperty("TrainNameEn")
    private String trainNameEn;

    @JsonProperty("TransportType")
    private String transportType;

    @JsonProperty("OriginName")
    private String originName;

    @JsonProperty("InitialStationName")
    private String initialStationName;

    @JsonProperty("OriginStationCode")
    private String originStationCode;

    @JsonProperty("OriginStationInfo")
    private StationInfo originStationInfo;

    @JsonProperty("InitialTrainStationInfo")
    private StationInfo initialTrainStationInfo;

    @JsonProperty("InitialTrainStationCode")
    private String initialTrainStationCode;

    @JsonProperty("InitialTrainStationCnsiCode")
    private String initialTrainStationCnsiCode;

    @JsonProperty("DestinationName")
    private String destinationName;

    @JsonProperty("FinalStationName")
    private String finalStationName;

    @JsonProperty("DestinationStationCode")
    private String destinationStationCode;

    @JsonProperty("DestinationStationInfo")
    private StationInfo destinationStationInfo;

    @JsonProperty("FinalTrainStationInfo")
    private StationInfo finalTrainStationInfo;

    @JsonProperty("FinalTrainStationCode")
    private String finalTrainStationCode;

    @JsonProperty("FinalTrainStationCnsiCode")
    private String finalTrainStationCnsiCode;

    @JsonProperty("DestinationNames")
    private List<String> destinationNames;

    @JsonProperty("FinalStationNames")
    private List<String> finalStationNames;

    @JsonProperty("DepartureDateTime")
    private String departureDateTime;

    @JsonProperty("LocalDepartureDateTime")
    private String localDepartureDateTime;

    @JsonProperty("ArrivalDateTime")
    private String arrivalDateTime;

    @JsonProperty("LocalArrivalDateTime")
    private String localArrivalDateTime;

    @JsonProperty("ArrivalDateTimes")
    private List<String> arrivalDateTimes;

    @JsonProperty("LocalArrivalDateTimes")
    private List<String> localArrivalDateTimes;

    @JsonProperty("DepartureDateFromFormingStation")
    private String departureDateFromFormingStation;

    @JsonProperty("DepartureStopTime")
    private Integer departureStopTime;

    @JsonProperty("ArrivalStopTime")
    private Integer arrivalStopTime;

    @JsonProperty("TripDuration")
    private Double tripDuration;

    @JsonProperty("TripDistance")
    private Integer tripDistance;

    @JsonProperty("IsSuburban")
    private Boolean isSuburban;

    @JsonProperty("IsComponent")
    private Boolean isComponent;

    @JsonProperty("CarServices")
    private List<String> carServices;

    @JsonProperty("IsSaleForbidden")
    private Boolean isSaleForbidden;

    @JsonProperty("IsTicketPrintRequiredForBoarding")
    private Boolean isTicketPrintRequiredForBoarding;

    @JsonProperty("BookingSystem")
    private String bookingSystem;

    @JsonProperty("IsVrStorageSystem")
    private Boolean isVrStorageSystem;

    @JsonProperty("PlacesStorageType")
    private String placesStorageType;

    @JsonProperty("BoardingSystemTypes")
    private List<String> boardingSystemTypes;

    @JsonProperty("TrainBrandCode")
    private String trainBrandCode;

    @JsonProperty("TrainClassNames")
    private Object trainClassNames;

    @JsonProperty("ServiceProvider")
    private String serviceProvider;

    @JsonProperty("DestinationStationName")
    private String destinationStationName;

    @JsonProperty("OriginStationName")
    private String originStationName;

    @JsonProperty("IsPlaceRangeAllowed")
    private Boolean isPlaceRangeAllowed;

    @JsonProperty("IsTrainRouteAllowed")
    private Boolean isTrainRouteAllowed;
}
