package net.osandman.rzdmonitoring.client;

import org.springframework.util.MultiValueMap;

public interface RestConnector {

    <T> T callGetRequest(String url, MultiValueMap<String, String> params, Class<T> clazz);

    String callGetRequest(String url, MultiValueMap<String, String> params);
}
