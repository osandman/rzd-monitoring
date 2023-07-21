package net.osandman.rzdmonitoring.dto.train;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

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
