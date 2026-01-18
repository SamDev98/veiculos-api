package com.tinnova.veiculos.infraestrutura.cambio;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Cliente para API de cotação Frankfurter (fallback).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FrankfurterClient implements ClienteCambio {

    private static final String URL = "https://api.frankfurter.app/latest?from=USD&to=BRL";

    private final RestTemplate restTemplate;

    @Override
    public Optional<BigDecimal> obterCotacaoUsdBrl() {
        try {
            String resposta = restTemplate.getForObject(URL, String.class);

            ObjectMapper mapeador = new ObjectMapper();
            JsonNode raiz = mapeador.readTree(resposta);
            double cotacao = raiz.path("rates").path("BRL").asDouble();

            return Optional.of(BigDecimal.valueOf(cotacao));
        } catch (Exception e) {
            log.warn("Falha ao obter cotação da Frankfurter: {}", e.getMessage());
            return Optional.empty();
        }
    }
}