package net.osandman.rzdmonitoring.client.dto.v2.train;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class RootDto {

    @JsonProperty("OriginCode")
    private String originCode;

    @JsonProperty("DestinationCode")
    private String destinationCode;

    @JsonProperty("OriginTimeZoneDifference")
    private Integer originTimeZoneDifference;

    @JsonProperty("DestinationTimeZoneDifference")
    private Integer destinationTimeZoneDifference;

    @JsonProperty("Cars")
    private List<CarDto> cars;

    @JsonProperty("RoutePolicy")
    private String routePolicy;

    @JsonProperty("TrainInfo")
    private TrainInfoDto trainInfo;

    @JsonProperty("IsFromUkrain")
    private Boolean isFromUkrain;

    @JsonProperty("AllowedDocumentTypes")
    private List<String> allowedDocumentTypes;

    @JsonProperty("ClientFeeCalculation")
    private Object clientFeeCalculation; // Может быть null

    @JsonProperty("AgentFeeCalculation")
    private Object agentFeeCalculation; // Может быть null

    @JsonProperty("BookingSystem")
    private String bookingSystem;

    @JsonProperty("CarTariffPrices")
    private Object carTariffPrices; // Может быть null

    @JsonProperty("ProviderError")
    private String providerError;

    @JsonProperty("Code")
    private String code;

    @JsonProperty("Message")
    private String message;

    @JsonProperty("MessageParams")
    private List<String> messageParams;
}
