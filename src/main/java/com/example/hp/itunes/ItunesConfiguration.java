package com.example.hp.itunes;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ItunesConfiguration {

    @Bean
    public WebClient itunesWebClient() {
        return WebClient.builder()
                .baseUrl("https://itunes.apple.com")
                .build();
    }
}
