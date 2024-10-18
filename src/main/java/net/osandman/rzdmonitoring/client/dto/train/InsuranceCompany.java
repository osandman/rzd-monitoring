package net.osandman.rzdmonitoring.client.dto.train;

import lombok.Data;

@Data
public class InsuranceCompany {
    public int id;
    public String shortName;
    public String offerUrl;
    public int insuranceCost;
    public int insuranceBenefit;
    public int sortOrder;
}
