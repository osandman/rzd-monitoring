package net.osandman.rzdmonitoring.client.dto.train;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class InsuranceTariff {
    public int id;
    public String name;
    public int insuranceCost;
    public int insuranceBenefit;
    @JsonProperty("default")
    public boolean mydefault;
    @JsonProperty("InsurancePrograms")
    public List<InsuranceProgram> insurancePrograms;
}
