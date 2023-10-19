package net.osandman.rzdmonitoring.client.dto.train;

import java.util.List;

public class RootTrain {
    public String result;
    public List<Lst> lst;
    public List<Scheme> schemes;
    public List<InsuranceCompany> insuranceCompany;
    public List<InsuranceCompanyType> insuranceCompanyTypes;
    public Object psaction;
    public int childrenAge;
    public int motherAndChildAge;
    public boolean partialPayment;
    public String timestamp;
}
