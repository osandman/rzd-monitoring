package net.osandman.rzdmonitoring.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import static org.springframework.util.StringUtils.hasText;

@Service
@RequiredArgsConstructor
@Slf4j
@Primary
public class RestTemplateConnector implements RestConnector {

    public static final String HOST = "pass.rzd.ru";
    private static final HttpHeaders httpHeaders = new HttpHeaders();

    private final RestTemplate restTemplate;

    @Value("${rzd.base-url}")
    private String baseAppUrl;

    static {
//        httpHeaders.set("Host", HOST);
//        httpHeaders.set("Cookie", "JSESSIONID=4054C174E5CD78D5FDD8BD8D155FC233");
//        httpHeaders.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");
    }

    @Override
    public <T> T callGetRequest(String endpoint, MultiValueMap<String, String> params, Class<T> respClass) {
        return callExchange("", endpoint, params, respClass, HttpMethod.GET, null);
    }

    @Override
    public <T> T callGetRequest(String baseUrl, String endpoint, MultiValueMap<String, String> params, Class<T> respClass) {
        return callExchange(baseUrl, endpoint, params, respClass, HttpMethod.GET, null);
    }

    @Override
    public String callGetRequest(String endpoint, MultiValueMap<String, String> params) {
        return callExchange("", endpoint, params, String.class, HttpMethod.GET, null);
    }

    private <T> T callExchange(
        String baseUrl,
        @NonNull String endpoint,
        MultiValueMap<String, String> params,
        @NonNull Class<T> respClass,
        @NonNull HttpMethod httpMethod,
        String requestBody
    ) throws RestClientException, IllegalArgumentException {
        HttpEntity<String> requestHttpEntity = new HttpEntity<>(requestBody, httpHeaders);
        URI url = UriComponentsBuilder.fromHttpUrl(hasText(baseUrl) ? baseUrl : baseAppUrl)
            .path(endpoint)
            .queryParams(params)
            .build().encode().toUri();
        ResponseEntity<T> response = restTemplate.exchange(
            url,
            httpMethod,
            requestHttpEntity,
            respClass
        );
        log.info("Successful exchange, status code={} with url '{}'", response.getStatusCode(), url);
        return response.getBody();
    }
}
