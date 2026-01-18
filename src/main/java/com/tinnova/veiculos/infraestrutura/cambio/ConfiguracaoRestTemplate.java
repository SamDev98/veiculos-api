package com.tinnova.veiculos.infraestrutura.cambio;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ConfiguracaoRestTemplate {

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
