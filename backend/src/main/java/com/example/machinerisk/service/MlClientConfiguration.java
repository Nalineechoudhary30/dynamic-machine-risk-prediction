package com.example.machinerisk.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Configuration
public class MlClientConfiguration {
    @Bean RestClient mlRestClient(@Value("${ml.service.url:http://localhost:8000}") String mlUrl) {
        return RestClient.builder().baseUrl(mlUrl)
            .requestFactory(new SimpleClientHttpRequestFactory()).build();
    }
}
