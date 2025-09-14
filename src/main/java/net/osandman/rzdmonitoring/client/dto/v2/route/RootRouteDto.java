package net.osandman.rzdmonitoring.client.dto.v2.route;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class RootRouteDto {

    @JsonProperty("errorInfo")
    private ErrorInfo errorInfo;

    @JsonProperty("Trains")
    private List<Train> trains;

    @JsonProperty("OriginStationCode")
    private String originStationCode;

    @JsonProperty("OriginStationInfo")
    private StationInfo originStationInfo;

    @JsonProperty("OriginTimeZoneDifference")
    private Integer originTimeZoneDifference;

    @JsonProperty("DestinationCode")
    private String destinationCode;

    @JsonProperty("DestinationStationInfo")
    private StationInfo destinationStationInfo;

    @JsonProperty("DestinationTimeZoneDifference")
    private Integer destinationTimeZoneDifference;

    @JsonProperty("RoutePolicy")
    private String routePolicy;

    @JsonProperty("DepartureTimeDescription")
    private String departureTimeDescription;

    @JsonProperty("ArrivalTimeDescription")
    private String arrivalTimeDescription;

    @JsonProperty("IsFromUkrain")
    private Boolean isFromUkrain;

    @JsonProperty("NotAllTrainsReturned")
    private Boolean notAllTrainsReturned;

    @JsonProperty("BookingSystem")
    private String bookingSystem;

    @JsonProperty("Id")
    private Integer id;

    @JsonProperty("DestinationStationName")
    private String destinationStationName;

    @JsonProperty("OriginStationName")
    private String originStationName;

    @JsonProperty("MoscowDateTime")
    private String moscowDateTime;

    @JsonProperty("ClientFeeCalculation")
    private Object clientFeeCalculation;

    @JsonProperty("AgentFeeCalculation")
    private Object agentFeeCalculation;
}
