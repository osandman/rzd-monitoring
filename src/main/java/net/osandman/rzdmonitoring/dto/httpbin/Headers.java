package net.osandman.rzdmonitoring.dto.httpbin;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Headers {
    @JsonProperty("Accept")
    public String accept;
    @JsonProperty("Accept-Encoding")
    public String acceptEncoding;
    @JsonProperty("Accept-Language")
    public String acceptLanguage;
    @JsonProperty("Host")
    public String host;
    @JsonProperty("Sec-Ch-Ua")
    public String secChUa;
    @JsonProperty("Sec-Ch-Ua-Mobile")
    public String getSecChUaMobile;
    @JsonProperty("Sec-Ch-Ua-Platform")
    public String getSecChUaPlatform;
}
