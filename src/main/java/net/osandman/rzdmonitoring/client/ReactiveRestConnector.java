package net.osandman.rzdmonitoring.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class ReactiveRestConnector implements RestConnector {
    private final WebClient webClient;

    public ReactiveRestConnector(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public <T> T callGetRequest(String url, MultiValueMap<String, String> params, Class<T> respClass) {
        return webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path(url)
                .queryParams(params)
                .build())
            .header("Host", "pass.rzd.ru")
            .header("Content-Type", "text/javascript;charset=utf-8")
            .header("Connection", "keep-alive")
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                                  "(KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36")
            .header("Accept-Language", "ru")
            .retrieve()
            .bodyToMono(respClass)
            .doOnError(error -> log.error("An error has occurred {}", error.getMessage()))
            .onErrorResume(error -> Mono.just((T) new Object())) // TODO сделать правильно обработку ошибок
            .block();
    }

    @Override
    public String callGetRequest(String url, MultiValueMap<String, String> params) {
        return webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path(url)
                .queryParams(params)
                .build())
            .header("Host", "pass.rzd.ru")
            .retrieve()
            .bodyToMono(String.class)
            .doOnError(error -> log.error("An error has occurred {}", error.getMessage()))
            .onErrorResume(error -> Mono.just(error.getMessage()))
            .block();
    }
}
