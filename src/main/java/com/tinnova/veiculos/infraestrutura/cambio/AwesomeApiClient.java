package com.tinnova.veiculos.infraestrutura.cambio;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AwesomeApiClient implements ClienteCambio {

    private static final String URL = "https://economia.awesomeapi.com.br/json/last/USD-BRL";

    private final RestTemplate restTemplate;

    @Override
    public Optional<BigDecimal> obterCotacaoUsdBrl() {
        try {
            String resposta = restTemplate.getForObject(URL, String.class);

            ObjectMapper mapeador = new ObjectMapper();
            JsonNode raiz = mapeador.readTree(resposta);
            String cotacao = raiz.path("USDBRL").path("bid").asText();

            return Optional.of(new BigDecimal(cotacao));
        } catch (Exception e) {
            log.warn("Falha ao obter cotação da AwesomeAPI: {}", e.getMessage());
            return Optional.empty();
        }
    }

}
