package net.osandman.rzdmonitoring.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.eclipse.jetty.client.HttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JettyClientHttpRequestFactory;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.JettyClientHttpConnector;
import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static org.springframework.http.MediaType.ALL;

@Configuration
public class WebClientConfiguration {

    @Value("${rzd.base-url}")
    private String baseUrl;

    /**
     * Конфигурация ObjectMapper.
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(new JavaTimeModule())
            .configure(WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Bean
    public RestTemplate restTemplate() {
        JettyClientHttpRequestFactory jettyClientHttpRequestFactory = new JettyClientHttpRequestFactory();
        jettyClientHttpRequestFactory.setConnectTimeout(Duration.ofSeconds(10));
        RestTemplate restTemplate = new RestTemplate(jettyClientHttpRequestFactory);
        restTemplate.getInterceptors().add(new RestTemplateRequestInterceptor());
        return restTemplate;
    }

    @Bean
    WebClient webClient() {
        HttpClient httpClient = new HttpClient();
        ClientHttpConnector connector = new JettyClientHttpConnector(httpClient);
        return WebClient.builder()
            .baseUrl(baseUrl)
            .clientConnector(connector)
            .exchangeStrategies(ExchangeStrategies.builder().codecs(this::acceptedCodecs).build())
            .build();
    }

    private void acceptedCodecs(ClientCodecConfigurer clientCodecConfigurer) {
        clientCodecConfigurer.customCodecs().register(new Jackson2JsonEncoder(objectMapper(), ALL));
        clientCodecConfigurer.customCodecs().register(new Jackson2JsonDecoder(objectMapper(), ALL));
    }
}
