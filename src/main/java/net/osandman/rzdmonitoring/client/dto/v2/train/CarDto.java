package net.osandman.rzdmonitoring.client.dto.v2.train;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class CarDto {
    @JsonProperty("DestinationStationCode")
    private String destinationStationCode;

    @JsonProperty("CarType")
    private String carType;

    @JsonProperty("CarDirection")
    private String carDirection;

    @JsonProperty("RailwayCarSchemeId")
    private Integer railwayCarSchemeId;

    @JsonProperty("HasImages")
    private Boolean hasImages;

    @JsonProperty("CarSubType")
    private String carSubType;

    @JsonProperty("CarTypeName")
    private String carTypeName;

    @JsonProperty("CarSchemeName")
    private String carSchemeName;

    @JsonProperty("CarNumber")
    private String carNumber;

    @JsonProperty("ServiceClass")
    private String serviceClass;

    @JsonProperty("ServiceClassNameRu")
    private String serviceClassNameRu;

    @JsonProperty("ServiceClassNameEn")
    private String serviceClassNameEn;

    @JsonProperty("InternationalServiceClass")
    private String internationalServiceClass;

    @JsonProperty("CarDescription")
    private String carDescription;

    @JsonProperty("ServiceClassTranscript")
    private String serviceClassTranscript;

    @JsonProperty("FreePlaces")
    private String freePlaces;

    @JsonProperty("FreePlacesByCompartments")
    private List<FreePlacesByCompartmentsDto> freePlacesByCompartments;

    @JsonProperty("PlaceQuantity")
    private Integer placeQuantity;

    @JsonProperty("IsTwoStorey")
    private Boolean isTwoStorey;

    @JsonProperty("Services")
    private List<String> services;

    @JsonProperty("ServicesWithIndication")
    private List<ServicesWithIndicationDto> servicesWithIndication;

    @JsonProperty("PetTransportationShortDescription")
    private String petTransportationShortDescription;

    @JsonProperty("PetTransportationFullDescription")
    private String petTransportationFullDescription;

    @JsonProperty("MinPrice")
    private Double minPrice;

    @JsonProperty("MaxPrice")
    private Double maxPrice;

    @JsonProperty("ServiceCost")
    private Double serviceCost;

    @JsonProperty("PlaceReservationType")
    private String placeReservationType;

    @JsonProperty("Carrier")
    private String carrier;

    @JsonProperty("CarrierDisplayName")
    private String carrierDisplayName;

    @JsonProperty("HasGenderCabins")
    private Boolean hasGenderCabins;

    @JsonProperty("RzhdCardTypes")
    private List<String> rzhdCardTypes;

    @JsonProperty("TrainNumber")
    private String trainNumber;

    @JsonProperty("ArrivalDateTime")
    private String arrivalDateTime;

    @JsonProperty("LocalArrivalDateTime")
    private String localArrivalDateTime;

    @JsonProperty("HasNoInterchange")
    private Boolean hasNoInterchange;

    @JsonProperty("HasPlaceNumeration")
    private Boolean hasPlaceNumeration;

    @JsonProperty("IsBeddingSelectionPossible")
    private Boolean isBeddingSelectionPossible;

    @JsonProperty("HasElectronicRegistration")
    private Boolean hasElectronicRegistration;

    @JsonProperty("HasDynamicPricing")
    private Boolean hasDynamicPricing;

    @JsonProperty("HasPlacesNearBabies")
    private Boolean hasPlacesNearBabies;

    @JsonProperty("HasPlacesNearPlayground")
    private Boolean hasPlacesNearPlayground;

    @JsonProperty("HasPlacesNearPets")
    private Boolean hasPlacesNearPets;

    @JsonProperty("HasNonRefundableTariff")
    private Boolean hasNonRefundableTariff;

    @JsonProperty("OnlyNonRefundableTariff")
    private Boolean onlyNonRefundableTariff;

    @JsonProperty("IsAdditionalPassengerAllowed")
    private Boolean isAdditionalPassengerAllowed;

    @JsonProperty("IsChildTariffTypeAllowed")
    private Boolean isChildTariffTypeAllowed;

    @JsonProperty("CarPlaceType")
    private String carPlaceType;

    @JsonProperty("CarPlaceCode")
    private String carPlaceCode;

    @JsonProperty("CarPlaceNameRu")
    private String carPlaceNameRu;

    @JsonProperty("CarPlaceNameEn")
    private String carPlaceNameEn;

    @JsonProperty("Discounts")
    private List<DiscountDto> discounts;

    @JsonProperty("AllowedTariffs")
    private List<Object> allowedTariffs; // Тип уточняется

    @JsonProperty("IsSaleForbidden")
    private Boolean isSaleForbidden;

    @JsonProperty("AvailabilityIndication")
    private String availabilityIndication;

    @JsonProperty("IsThreeHoursReservationAvailable")
    private Boolean isThreeHoursReservationAvailable;

    @JsonProperty("Road")
    private String road;

    @JsonProperty("InfoRequestSchema")
    private String infoRequestSchema;

    @JsonProperty("PassengerSpecifyingRules")
    private String passengerSpecifyingRules;

    @JsonProperty("IsMealOptionPossible")
    private Boolean isMealOptionPossible;

    @JsonProperty("IsAdditionalMealOptionPossible")
    private Boolean isAdditionalMealOptionPossible;

    @JsonProperty("IsOnRequestMealOptionPossible")
    private Boolean isOnRequestMealOptionPossible;

    @JsonProperty("MealSalesOpenedTill")
    private String mealSalesOpenedTill;

    @JsonProperty("IsTransitDocumentRequired")
    private Boolean isTransitDocumentRequired;

    @JsonProperty("IsInterstate")
    private Boolean isInterstate;

    @JsonProperty("ClientFeeCalculation")
    private Object clientFeeCalculation; // Может быть null

    @JsonProperty("AgentFeeCalculation")
    private Object agentFeeCalculation; // Может быть null

    @JsonProperty("IsBranded")
    private Boolean isBranded;

    @JsonProperty("IsBuffet")
    private Boolean isBuffet;

    @JsonProperty("TripDirection")
    private String tripDirection;

    @JsonProperty("IsFromUkrainianCalcCenter")
    private Boolean isFromUkrainianCalcCenter;

    @JsonProperty("IsForDisabledPersons")
    private Boolean isForDisabledPersons;

    @JsonProperty("IsSpecialSaleMode")
    private Boolean isSpecialSaleMode;

    @JsonProperty("BoardingSystemType")
    private String boardingSystemType;

    @JsonProperty("AvailableBaggageTypes")
    private List<AvailableBaggageTypesDto> availableBaggageTypes;

    @JsonProperty("IsTourPackageAvailable")
    private Boolean isTourPackageAvailable;

    @JsonProperty("ArePlacesForBusinessTravelBooking")
    private Boolean arePlacesForBusinessTravelBooking;

    @JsonProperty("IsCarTransportationCoach")
    private Boolean isCarTransportationCoach;

    @JsonProperty("CarNumeration")
    private String carNumeration;

    @JsonProperty("IsGroupTransportaionAvailable")
    private Boolean isGroupTransportaionAvailable;

    @JsonProperty("IsAutoBookingAvailable")
    private Boolean isAutoBookingAvailable;

    @JsonProperty("TrainNumberFromFormingStation")
    private String trainNumberFromFormingStation;

    @JsonProperty("PlacesWithConditionalRefundableTariffQuantity")
    private Integer placesWithConditionalRefundableTariffQuantity;

    @JsonProperty("HasPlacesWithChild")
    private Boolean hasPlacesWithChild;

    @JsonProperty("HasPlacesForLargeFamily")
    private Boolean hasPlacesForLargeFamily;

    @JsonProperty("CarPlaceName")
    private String carPlaceName;

    @JsonProperty("HasFssBenefit")
    private Boolean hasFssBenefit;

    @JsonProperty("ServiceClassName")
    private String serviceClassName;
}
