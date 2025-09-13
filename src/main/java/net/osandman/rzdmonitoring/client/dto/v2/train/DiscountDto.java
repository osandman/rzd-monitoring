package net.osandman.rzdmonitoring.client.dto.v2.train;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DiscountDto {
    @JsonProperty("DiscountType")
    private String discountType;

    @JsonProperty("Description")
    private String description;
}
