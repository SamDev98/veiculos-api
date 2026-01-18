package com.tinnova.veiculos.dominio.veiculo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("RepositorioVeiculo")
class RepositorioVeiculoTest {

    @Autowired
    private RepositorioVeiculo repositorio;

    private Veiculo veiculoAtivo;
    private Veiculo veiculoInativo;

    @BeforeEach
    void setUp() {
        repositorio.deleteAll();

        veiculoAtivo = Veiculo.builder()
                .placa("ABC1234")
                .marca("Toyota")
                .modelo("Corolla")
                .ano(2023)
                .cor("Preto")
                .precoUsd(new BigDecimal("25000"))
                .ativo(true)
                .build();

        veiculoInativo = Veiculo.builder()
                .placa("XYZ9999")
                .marca("Honda")
                .modelo("Civic")
                .ano(2022)
                .cor("Branco")
                .precoUsd(new BigDecimal("22000"))
                .ativo(false)
                .build();

        repositorio.save(veiculoAtivo);
        repositorio.save(veiculoInativo);
    }

    @Nested
    @DisplayName("findByIdAndAtivoTrue()")
    class FindByIdAndAtivoTrue {

        @Test
        @DisplayName("deve retornar veiculo ativo")
        void deveRetornarVeiculoAtivo() {
            // when
            var resultado = repositorio.findByIdAndAtivoTrue(veiculoAtivo.getId());

            // then
            assertThat(resultado).isPresent();
            assertThat(resultado.get().getPlaca()).isEqualTo("ABC1234");
        }

        @Test
        @DisplayName("nao deve retornar veiculo inativo")
        void naoDeveRetornarVeiculoInativo() {
            // when
            var resultado = repositorio.findByIdAndAtivoTrue(veiculoInativo.getId());

            // then
            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("deve retornar vazio para id inexistente")
        void deveRetornarVazioParaIdInexistente() {
            // when
            var resultado = repositorio.findByIdAndAtivoTrue(999L);

            // then
            assertThat(resultado).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByPlaca()")
    class ExistsByPlaca {

        @Test
        @DisplayName("deve retornar true quando placa existe")
        void deveRetornarTrueQuandoPlacaExiste() {
            // when
            var existe = repositorio.existsByPlaca("ABC1234");

            // then
            assertThat(existe).isTrue();
        }

        @Test
        @DisplayName("deve retornar false quando placa nao existe")
        void deveRetornarFalseQuandoPlacaNaoExiste() {
            // when
            var existe = repositorio.existsByPlaca("NAO_EXISTE");

            // then
            assertThat(existe).isFalse();
        }
    }

    @Nested
    @DisplayName("existsByPlacaAndIdNot()")
    class ExistsByPlacaAndIdNot {

        @Test
        @DisplayName("deve retornar true quando placa existe em outro veiculo")
        void deveRetornarTrueQuandoPlacaExisteEmOutroVeiculo() {
            // when
            var existe = repositorio.existsByPlacaAndIdNot("ABC1234", veiculoInativo.getId());

            // then
            assertThat(existe).isTrue();
        }

        @Test
        @DisplayName("deve retornar false quando placa pertence ao mesmo veiculo")
        void deveRetornarFalseQuandoPlacaPertenceAoMesmoVeiculo() {
            // when
            var existe = repositorio.existsByPlacaAndIdNot("ABC1234", veiculoAtivo.getId());

            // then
            assertThat(existe).isFalse();
        }
    }

    @Nested
    @DisplayName("contarPorMarca()")
    class ContarPorMarca {

        @Test
        @DisplayName("deve retornar contagem agrupada por marca")
        void deveRetornarContagemAgrupadaPorMarca() {
            // given
            repositorio.save(Veiculo.builder()
                    .placa("DEF5678")
                    .marca("Toyota")
                    .modelo("Hilux")
                    .ano(2023)
                    .cor("Prata")
                    .precoUsd(new BigDecimal("45000"))
                    .ativo(true)
                    .build());

            // when
            List<Object[]> resultado = repositorio.contarPorMarca();

            // then
            assertThat(resultado).isNotEmpty();
            var toyota = resultado.stream()
                    .filter(r -> "Toyota".equals(r[0]))
                    .findFirst();
            assertThat(toyota).isPresent();
            assertThat(toyota.get()[1]).isEqualTo(2L);
        }

        @Test
        @DisplayName("nao deve contar veiculos inativos")
        void naoDeveContarVeiculosInativos() {
            // when
            List<Object[]> resultado = repositorio.contarPorMarca();

            // then
            var honda = resultado.stream()
                    .filter(r -> "Honda".equals(r[0]))
                    .findFirst();
            assertThat(honda).isEmpty();
        }
    }

    @Nested
    @DisplayName("Constraint de placa unica")
    class ConstraintPlacaUnica {

        @Test
        @DisplayName("deve garantir unicidade de placa")
        void deveGarantirUnicidadeDePlaca() {
            // given
            assertThat(repositorio.existsByPlaca("ABC1234")).isTrue();

            // when
            Veiculo novoVeiculo = Veiculo.builder()
                    .placa("NEW1234")
                    .marca("Ford")
                    .modelo("Focus")
                    .ano(2021)
                    .cor("Azul")
                    .precoUsd(new BigDecimal("18000"))
                    .ativo(true)
                    .build();

            var salvo = repositorio.save(novoVeiculo);

            // then
            assertThat(salvo.getId()).isNotNull();
        }
    }
}
