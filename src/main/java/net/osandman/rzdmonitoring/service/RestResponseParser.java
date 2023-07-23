package net.osandman.rzdmonitoring.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class RestResponseParser {

    private final WebClient webClient;

    private final Logger logger = LoggerFactory.getLogger(RestResponseParser.class);

    public RestResponseParser(WebClient webClient) {
        this.webClient = webClient;
    }

    public <T> T callSecondGetRequest(String url, MultiValueMap<String, String> params, Class<T> clazz) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(url)
//                        .queryParam("tnum0", "109М")
                        .queryParams(params)
                        .build())
//                .acceptCharset(StandardCharsets.UTF_8)
                .header("Host", "pass.rzd.ru")
//                .header("Content-Type", "text/javascript;charset=utf-8")
//                .accept(MediaType.APPLICATION_JSON)
//                .header("Connection", "keep-alive")
//                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
//                .header("Accept-Language", "ru")
//                .header("Accept", "application/json", "text/plain")
//                .cookie("AuthFlag", "false")
//                .cookie("lang", "ru")
                .retrieve()
                .bodyToMono(clazz)
                .doOnError(error -> logger.error("An error has occurred {}", error.getMessage()))
                .onErrorResume(error -> Mono.just((T) new Object()))
                .block();
    }

    public String callSecondGetRequest(String url, MultiValueMap<String, String> params) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(url)
                        .queryParams(params)
                        .build())
//                .acceptCharset(StandardCharsets.UTF_8)
                .header("Host", "pass.rzd.ru")
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(error -> logger.error("An error has occurred {}", error.getMessage()))
                .onErrorResume(error -> Mono.just(error.getMessage()))
                .block();
    }
}
