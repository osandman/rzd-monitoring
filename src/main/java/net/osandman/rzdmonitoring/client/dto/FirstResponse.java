package net.osandman.rzdmonitoring.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FirstResponse {
    private String result;
    @JsonProperty("RID")
    private long RID;
    private String timestamp;
}
