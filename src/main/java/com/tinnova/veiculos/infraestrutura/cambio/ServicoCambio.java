package com.tinnova.veiculos.infraestrutura.cambio;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServicoCambio {

    private static final String CACHE_KEY = "cotacao:usd:brl";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    private final AwesomeApiClient awesomeApiClient;
    private final FrankfurterClient frankfurterClient;
    private final RedisTemplate<String, String> redisTemplate;

    public BigDecimal obterCotacaoUsdBrl() {
        String cacheado = redisTemplate.opsForValue().get(CACHE_KEY);
        if (cacheado != null) {
            log.info("Cotação obtida do cache: {}", cacheado);
            return new BigDecimal(cacheado);
        }

        Optional<BigDecimal> cotacao = awesomeApiClient.obterCotacaoUsdBrl();
        if (cotacao.isPresent()) {
            salvarNoCache(cotacao.get());
            return cotacao.get();
        }

        cotacao = frankfurterClient.obterCotacaoUsdBrl();
        if (cotacao.isPresent()) {
            salvarNoCache(cotacao.get());
            return cotacao.get();
        }

        throw new RuntimeException("Não foi possível obter cotação USD/BRL");
    }

    public BigDecimal converterUsdParaBrl(BigDecimal valorUsd) {
        return valorUsd.multiply(obterCotacaoUsdBrl());
    }

    private void salvarNoCache(BigDecimal cotacao) {
        redisTemplate.opsForValue().set(CACHE_KEY, cotacao.toString(), CACHE_TTL);
        log.info("Cotação salva no cache: {} (TTL: {})", cotacao, CACHE_TTL);
    }

}
