package com.ProductModule.config;

import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.modelmapper.ModelMapper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.netty.http.client.HttpClient;
import java.time.Duration;

@Configuration
public class config {
    @Bean
    public ModelMapper getmodel(){
        return new ModelMapper();
    }

    /**
     * setConnectTimeout(Duration.ofSeconds(5)): Sets the connection timeout to 5 seconds. This is the maximum time it will wait for a connection to be established with the remote server.
     * setReadTimeout(Duration.ofSeconds(3)): Sets the read timeout to 3 seconds, which is the maximum time to wait for data to be read after the connection has been established.
     * @param builder
     * @return
     */
    @Bean
    public RestTemplate getRestTemplate(RestTemplateBuilder builder){
        return builder.setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(3))
                .build();
    }
//    public WebClient webClient() {
//        return WebClient.builder()
//                .baseUrl("https://your-api-url.com")  // Optional base URL
//                .timeout(Duration.ofSeconds(5), Duration.ofSeconds(3))
//                .build();
//    }

    /**
     * spring-boot-starter-webflux: Adds Spring WebFlux for reactive programming and non-blocking I/O, enabling WebClient for making asynchronous HTTP calls.
     * reactor-netty-http: Provides the Netty-based HTTP client, which is essential for non-blocking, high-performance HTTP communication with WebClient.
     * netty-handler: Contains essential classes like ReadTimeoutHandler and WriteTimeoutHandler, which allow you to set read and write timeouts on connections
     * @return
     */
    @Bean
    public WebClient webClient() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(5)) // Total response timeout
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(5)) // Read timeout
                                .addHandlerLast(new WriteTimeoutHandler(3)) // Write timeout
                );

        return WebClient.builder()
                .baseUrl("https://your-api-url.com")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
