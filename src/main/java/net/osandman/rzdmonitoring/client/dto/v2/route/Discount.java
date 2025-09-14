package net.osandman.rzdmonitoring.client.dto.v2.route;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Discount {

    @JsonProperty("DiscountType")
    private String discountType;

    @JsonProperty("Description")
    private String description;
}
