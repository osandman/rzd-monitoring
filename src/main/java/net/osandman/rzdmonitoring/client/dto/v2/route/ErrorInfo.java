package net.osandman.rzdmonitoring.client.dto.v2.route;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ErrorInfo {

    @JsonProperty("ProviderError")
    private Object providerError;

    @JsonProperty("Code")
    private Integer code;

    @JsonProperty("Message")
    private String message;

    @JsonProperty("MessageParams")
    private List<Object> messageParams;
}
