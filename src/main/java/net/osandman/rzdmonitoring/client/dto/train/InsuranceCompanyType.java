package net.osandman.rzdmonitoring.client.dto.train;

import lombok.Data;

import java.util.List;

@Data
public class InsuranceCompanyType {
    public int typeId;
    public List<InsuranceTariff> insuranceTariffs;
}
