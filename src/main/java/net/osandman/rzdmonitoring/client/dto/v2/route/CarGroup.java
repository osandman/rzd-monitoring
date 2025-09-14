package net.osandman.rzdmonitoring.client.dto.v2.route;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class CarGroup {

    @JsonProperty("Carriers")
    private List<String> carriers;

    @JsonProperty("CarrierDisplayNames")
    private List<String> carrierDisplayNames;

    @JsonProperty("ServiceClasses")
    private List<String> serviceClasses;

    @JsonProperty("MinPrice")
    private Double minPrice;

    @JsonProperty("MaxPrice")
    private Double maxPrice;

    @JsonProperty("HasPlacesWithChild")
    private Boolean hasPlacesWithChild;

    @JsonProperty("HasPlacesForLargeFamily")
    private Boolean hasPlacesForLargeFamily;

    @JsonProperty("CarType")
    private String carType;

    @JsonProperty("CarTypeName")
    private String carTypeName;

    @JsonProperty("PlaceQuantity")
    private Integer placeQuantity;

    @JsonProperty("LowerPlaceQuantity")
    private Integer lowerPlaceQuantity;

    @JsonProperty("UpperPlaceQuantity")
    private Integer upperPlaceQuantity;

    @JsonProperty("LowerSidePlaceQuantity")
    private Integer lowerSidePlaceQuantity;

    @JsonProperty("UpperSidePlaceQuantity")
    private Integer upperSidePlaceQuantity;

    @JsonProperty("PlacesWithConditionalRefundableTariffQuantity")
    private Integer placesWithConditionalRefundableTariffQuantity;

    @JsonProperty("LowerPlacesWithConditionalRefundableTariffQuantity")
    private Integer lowerPlacesWithConditionalRefundableTariffQuantity;

    @JsonProperty("UpperPlacesWithConditionalRefundableTariffQuantity")
    private Integer upperPlacesWithConditionalRefundableTariffQuantity;

    @JsonProperty("MalePlaceQuantity")
    private Integer malePlaceQuantity;

    @JsonProperty("FemalePlaceQuantity")
    private Integer femalePlaceQuantity;

    @JsonProperty("EmptyCabinQuantity")
    private Integer emptyCabinQuantity;

    @JsonProperty("MixedCabinQuantity")
    private Integer mixedCabinQuantity;

    @JsonProperty("IsSaleForbidden")
    private Boolean isSaleForbidden;

    @JsonProperty("AvailabilityIndication")
    private String availabilityIndication;

    @JsonProperty("CarDescriptions")
    private List<String> carDescriptions;

    @JsonProperty("ServiceClassNameRu")
    private String serviceClassNameRu;

    @JsonProperty("ServiceClassNameEn")
    private String serviceClassNameEn;

    @JsonProperty("InternationalServiceClasses")
    private List<String> internationalServiceClasses;

    @JsonProperty("ServiceCosts")
    private List<Double> serviceCosts;

    @JsonProperty("IsBeddingSelectionPossible")
    private Boolean isBeddingSelectionPossible;

    @JsonProperty("BoardingSystemTypes")
    private List<String> boardingSystemTypes;

    @JsonProperty("HasElectronicRegistration")
    private Boolean hasElectronicRegistration;

    @JsonProperty("HasGenderCabins")
    private Boolean hasGenderCabins;

    @JsonProperty("HasPlaceNumeration")
    private Boolean hasPlaceNumeration;

    @JsonProperty("HasPlacesNearPlayground")
    private Boolean hasPlacesNearPlayground;

    @JsonProperty("HasPlacesNearPets")
    private Boolean hasPlacesNearPets;

    @JsonProperty("HasPlacesForDisabledPersons")
    private Boolean hasPlacesForDisabledPersons;

    @JsonProperty("HasPlacesNearBabies")
    private Boolean hasPlacesNearBabies;

    @JsonProperty("AvailableBaggageTypes")
    private List<BaggageType> availableBaggageTypes;

    @JsonProperty("HasNonRefundableTariff")
    private Boolean hasNonRefundableTariff;

    @JsonProperty("Discounts")
    private List<Discount> discounts;

    @JsonProperty("AllowedTariffs")
    private List<Object> allowedTariffs;

    @JsonProperty("InfoRequestSchema")
    private String infoRequestSchema;

    @JsonProperty("TotalPlaceQuantity")
    private Integer totalPlaceQuantity;

    @JsonProperty("PlaceReservationTypes")
    private List<String> placeReservationTypes;

    @JsonProperty("IsThreeHoursReservationAvailable")
    private Boolean isThreeHoursReservationAvailable;

    @JsonProperty("IsMealOptionPossible")
    private Boolean isMealOptionPossible;

    @JsonProperty("IsAdditionalMealOptionPossible")
    private Boolean isAdditionalMealOptionPossible;

    @JsonProperty("IsOnRequestMealOptionPossible")
    private Boolean isOnRequestMealOptionPossible;

    @JsonProperty("IsTransitDocumentRequired")
    private Boolean isTransitDocumentRequired;

    @JsonProperty("IsInterstate")
    private Boolean isInterstate;

    @JsonProperty("ClientFeeCalculation")
    private Object clientFeeCalculation;

    @JsonProperty("AgentFeeCalculation")
    private Object agentFeeCalculation;

    @JsonProperty("HasNonBrandedCars")
    private Boolean hasNonBrandedCars;

    @JsonProperty("TripPointQuantity")
    private Integer tripPointQuantity;

    @JsonProperty("HasPlacesForBusinessTravelBooking")
    private Boolean hasPlacesForBusinessTravelBooking;

    @JsonProperty("IsCarTransportationCoaches")
    private Boolean isCarTransportationCoaches;

    @JsonProperty("IsGroupTransportaionAvailable")
    private Boolean isGroupTransportaionAvailable;

    @JsonProperty("ServiceClassName")
    private String serviceClassName;

    @JsonProperty("HasFssBenefit")
    private Boolean hasFssBenefit;
}
