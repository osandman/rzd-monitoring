package net.osandman.rzdmonitoring.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.eclipse.jetty.client.HttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.JettyClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.springframework.http.MediaType.*;

@Configuration
public class WebClientConfiguration {
    public static final String BASE_URL = "https://pass.rzd.ru/timetable/public/ru?";
    //    public static final String BASE_URL = "https://httpbin.org/ip";
    public static final int TIMEOUT = 10_000;

    @Bean
    WebClient webClient() {
//        HttpClient httpClient = HttpClient.create()
//                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, TIMEOUT)
//                .responseTimeout(Duration.ofMillis(TIMEOUT))
//                .doOnConnected(conn ->
//                        conn.addHandlerLast(new ReadTimeoutHandler(TIMEOUT, TimeUnit.MILLISECONDS))
//                                .addHandlerLast(new WriteTimeoutHandler(TIMEOUT, TimeUnit.MILLISECONDS)));

        HttpClient httpClient = new HttpClient();
        ClientHttpConnector connector = new JettyClientHttpConnector(httpClient);
        return WebClient.builder()
                .baseUrl(BASE_URL)
                .clientConnector(connector)
                .exchangeStrategies(ExchangeStrategies.builder().codecs(this::acceptedCodecs).build())
                .build();
    }

    private void acceptedCodecs(ClientCodecConfigurer clientCodecConfigurer) {
        clientCodecConfigurer.customCodecs().register(new Jackson2JsonEncoder(new ObjectMapper(), ALL));
        clientCodecConfigurer.customCodecs().register(new Jackson2JsonDecoder(new ObjectMapper(), ALL));
    }

}
