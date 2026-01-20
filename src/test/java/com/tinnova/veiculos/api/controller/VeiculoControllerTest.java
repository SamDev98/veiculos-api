package com.tinnova.veiculos.api.controller;

import com.tinnova.veiculos.api.dto.VeiculoRequest;
import com.tinnova.veiculos.aplicacao.veiculo.ServicoVeiculo;
import com.tinnova.veiculos.dominio.veiculo.PlacaDuplicadaException;
import com.tinnova.veiculos.dominio.veiculo.Veiculo;
import com.tinnova.veiculos.dominio.veiculo.VeiculoNaoEncontradoException;
import com.tinnova.veiculos.infraestrutura.cambio.ServicoCambio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("VeiculoController")
class VeiculoControllerTest {

    @Mock
    private ServicoVeiculo servicoVeiculo;

    @Mock
    private ServicoCambio servicoCambio;

    @InjectMocks
    private VeiculoController controller;

    private Veiculo veiculoExemplo;
    private VeiculoRequest requisicaoValida;

    @BeforeEach
    void setUp() {
        lenient().when(servicoCambio.obterCotacaoUsdBrl()).thenReturn(new BigDecimal("5.00"));

        veiculoExemplo = Veiculo.builder()
                .id(1L)
                .placa("ABC1234")
                .marca("Toyota")
                .modelo("Corolla")
                .ano(2023)
                .cor("Preto")
                .precoUsd(new BigDecimal("25000"))
                .ativo(true)
                .build();

        requisicaoValida = new VeiculoRequest();
        requisicaoValida.setPlaca("ABC1234");
        requisicaoValida.setMarca("Toyota");
        requisicaoValida.setModelo("Corolla");
        requisicaoValida.setAno(2023);
        requisicaoValida.setCor("Preto");
        requisicaoValida.setPrecoUsd(new BigDecimal("25000"));
    }

    @Nested
    @DisplayName("listar()")
    class Listar {

        @Test
        @DisplayName("deve retornar pagina de veiculos")
        void deveRetornarPaginaDeVeiculos() {
            // given
            var pageable = PageRequest.of(0, 10);
            when(servicoVeiculo.listar(any(), any(), any(), any(), any(), any()))
                    .thenReturn(new PageImpl<>(List.of(veiculoExemplo)));

            // when
            var resposta = controller.listar(null, null, null, null, null, pageable);

            // then
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody().getContent()).hasSize(1);
        }

        @Test
        @DisplayName("deve aplicar filtros na listagem")
        void deveAplicarFiltrosNaListagem() {
            // given
            var pageable = PageRequest.of(0, 10);
            when(servicoVeiculo.listar(eq("Toyota"), eq(2023), eq("Preto"), any(), any(), any()))
                    .thenReturn(new PageImpl<>(List.of(veiculoExemplo)));

            // when
            var resposta = controller.listar("Toyota", 2023, "Preto", null, null, pageable);

            // then
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(servicoVeiculo).listar(eq("Toyota"), eq(2023), eq("Preto"), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("buscarPorId()")
    class BuscarPorId {

        @Test
        @DisplayName("deve retornar veiculo quando encontrado")
        void deveRetornarVeiculoQuandoEncontrado() {
            // given
            when(servicoVeiculo.buscarPorId(1L)).thenReturn(Optional.of(veiculoExemplo));

            // when
            var resposta = controller.buscarPorId(1L);

            // then
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody().getPlaca()).isEqualTo("ABC1234");
        }

        @Test
        @DisplayName("deve lancar excecao quando nao encontrado")
        void deveLancarExcecaoQuandoNaoEncontrado() {
            // given
            when(servicoVeiculo.buscarPorId(99L)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> controller.buscarPorId(99L))
                    .isInstanceOf(VeiculoNaoEncontradoException.class);
        }
    }

    @Nested
    @DisplayName("criar()")
    class Criar {

        @Test
        @DisplayName("deve criar veiculo e retornar 201")
        void deveCriarVeiculoERetornar201() {
            // given
            when(servicoVeiculo.criar(any(Veiculo.class))).thenReturn(veiculoExemplo);

            // when
            var resposta = controller.criar(requisicaoValida);

            // then
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(resposta.getBody().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("deve lancar excecao quando placa duplicada")
        void deveLancarExcecaoQuandoPlacaDuplicada() {
            // given
            when(servicoVeiculo.criar(any(Veiculo.class)))
                    .thenThrow(new PlacaDuplicadaException("ABC1234"));

            // when/then
            assertThatThrownBy(() -> controller.criar(requisicaoValida))
                    .isInstanceOf(PlacaDuplicadaException.class);
        }
    }

    @Nested
    @DisplayName("atualizar()")
    class Atualizar {

        @Test
        @DisplayName("deve atualizar veiculo e retornar 200")
        void deveAtualizarVeiculoERetornar200() {
            // given
            when(servicoVeiculo.atualizar(eq(1L), any(Veiculo.class))).thenReturn(veiculoExemplo);

            // when
            var resposta = controller.atualizar(1L, requisicaoValida);

            // then
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("deve lancar excecao quando veiculo nao encontrado")
        void deveLancarExcecaoQuandoVeiculoNaoEncontrado() {
            // given
            when(servicoVeiculo.atualizar(eq(99L), any(Veiculo.class)))
                    .thenThrow(new VeiculoNaoEncontradoException(99L));

            // when/then
            assertThatThrownBy(() -> controller.atualizar(99L, requisicaoValida))
                    .isInstanceOf(VeiculoNaoEncontradoException.class);
        }
    }

    @Nested
    @DisplayName("atualizarParcial()")
    class AtualizarParcial {

        @Test
        @DisplayName("deve atualizar parcialmente e retornar 200")
        void deveAtualizarParcialmenteERetornar200() {
            // given
            when(servicoVeiculo.atualizarParcial(eq(1L), any(Veiculo.class))).thenReturn(veiculoExemplo);

            // when
            var resposta = controller.atualizarParcial(1L, requisicaoValida);

            // then
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("remover()")
    class Remover {

        @Test
        @DisplayName("deve remover e retornar 204")
        void deveRemoverERetornar204() {
            // when
            var resposta = controller.remover(1L);

            // then
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(servicoVeiculo).remover(1L);
        }
    }

    @Nested
    @DisplayName("relatorioPorMarca()")
    class RelatorioPorMarca {

        @Test
        @DisplayName("deve retornar relatorio por marca")
        void deveRetornarRelatorioPorMarca() {
            // given
            List<Object[]> contagem = List.of(
                    new Object[]{"Toyota", 2L},
                    new Object[]{"Honda", 1L}
            );
            when(servicoVeiculo.contarPorMarca()).thenReturn(contagem);

            // when
            var resposta = controller.relatorioPorMarca();

            // then
            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).hasSize(2);
        }
    }
}
