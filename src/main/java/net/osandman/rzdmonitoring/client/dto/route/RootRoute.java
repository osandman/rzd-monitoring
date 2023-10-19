package net.osandman.rzdmonitoring.client.dto.route;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;

import java.util.List;

@Data
@JsonTypeName("Root")
public class RootRoute {
    public String result;
    public List<Tp> tp;
    @JsonProperty("TransferSearchMode")
    public String transferSearchMode;
    public boolean flFPKRoundBonus;
    @JsonProperty("AutoTransferMode")
    public boolean autoTransferMode;
    public Discounts discounts;
    public String timestamp;
    public long RID; //TODO разобраться когда это поле здесь появляется
}
