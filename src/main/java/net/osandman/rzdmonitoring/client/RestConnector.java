package net.osandman.rzdmonitoring.client;

import org.springframework.util.MultiValueMap;

public interface RestConnector {

    <T> T callGetRequest(String url, MultiValueMap<String, String> params, Class<T> respClass);

    default <T> T callGetRequest(String basePath, String url, MultiValueMap<String, String> params, Class<T> respClass) {
        return callGetRequest(url, params, respClass);
    }

    String callGetRequest(String url, MultiValueMap<String, String> params);
}
