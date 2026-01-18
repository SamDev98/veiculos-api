package com.tinnova.veiculos.infraestrutura.cambio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ServicoCambio")
class ServicoCambioTest {

    @Mock
    private AwesomeApiClient awesomeApiClient;

    @Mock
    private FrankfurterClient frankfurterClient;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> operacoesValor;

    private ServicoCambio servicoCambio;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(operacoesValor);
        servicoCambio = new ServicoCambio(awesomeApiClient, frankfurterClient, redisTemplate);
    }

    @Nested
    @DisplayName("obterCotacaoUsdBrl()")
    class ObterCotacaoUsdBrl {

        @Test
        @DisplayName("deve retornar cotacao do cache quando disponivel")
        void deveRetornarCotacaoDoCacheQuandoDisponivel() {
            // given
            when(operacoesValor.get("cotacao:usd:brl")).thenReturn("5.50");

            // when
            BigDecimal resultado = servicoCambio.obterCotacaoUsdBrl();

            // then
            assertThat(resultado).isEqualByComparingTo(new BigDecimal("5.50"));
            verify(awesomeApiClient, never()).obterCotacaoUsdBrl();
            verify(frankfurterClient, never()).obterCotacaoUsdBrl();
        }

        @Test
        @DisplayName("deve buscar da AwesomeAPI quando cache vazio")
        void deveBuscarDaAwesomeApiQuandoCacheVazio() {
            // given
            when(operacoesValor.get("cotacao:usd:brl")).thenReturn(null);
            when(awesomeApiClient.obterCotacaoUsdBrl()).thenReturn(Optional.of(new BigDecimal("5.25")));

            // when
            BigDecimal resultado = servicoCambio.obterCotacaoUsdBrl();

            // then
            assertThat(resultado).isEqualByComparingTo(new BigDecimal("5.25"));
            verify(operacoesValor).set(eq("cotacao:usd:brl"), eq("5.25"), any(Duration.class));
            verify(frankfurterClient, never()).obterCotacaoUsdBrl();
        }

        @Test
        @DisplayName("deve usar fallback Frankfurter quando AwesomeAPI falha")
        void deveUsarFallbackFrankfurterQuandoAwesomeApiFalha() {
            // given
            when(operacoesValor.get("cotacao:usd:brl")).thenReturn(null);
            when(awesomeApiClient.obterCotacaoUsdBrl()).thenReturn(Optional.empty());
            when(frankfurterClient.obterCotacaoUsdBrl()).thenReturn(Optional.of(new BigDecimal("5.30")));

            // when
            BigDecimal resultado = servicoCambio.obterCotacaoUsdBrl();

            // then
            assertThat(resultado).isEqualByComparingTo(new BigDecimal("5.30"));
            verify(operacoesValor).set(eq("cotacao:usd:brl"), eq("5.30"), any(Duration.class));
        }

        @Test
        @DisplayName("deve lancar excecao quando ambas APIs falham")
        void deveLancarExcecaoQuandoAmbasApisFalham() {
            // given
            when(operacoesValor.get("cotacao:usd:brl")).thenReturn(null);
            when(awesomeApiClient.obterCotacaoUsdBrl()).thenReturn(Optional.empty());
            when(frankfurterClient.obterCotacaoUsdBrl()).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> servicoCambio.obterCotacaoUsdBrl())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Não foi possível obter cotação USD/BRL");
        }
    }

    @Nested
    @DisplayName("converterUsdParaBrl()")
    class ConverterUsdParaBrl {

        @Test
        @DisplayName("deve converter valor corretamente")
        void deveConverterValorCorretamente() {
            // given
            when(operacoesValor.get("cotacao:usd:brl")).thenReturn("5.00");

            // when
            BigDecimal resultado = servicoCambio.converterUsdParaBrl(new BigDecimal("100"));

            // then
            assertThat(resultado).isEqualByComparingTo(new BigDecimal("500"));
        }
    }
}
