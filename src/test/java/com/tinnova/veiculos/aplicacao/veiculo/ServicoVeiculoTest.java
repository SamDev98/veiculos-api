package com.tinnova.veiculos.aplicacao.veiculo;

import com.tinnova.veiculos.dominio.veiculo.PlacaDuplicadaException;
import com.tinnova.veiculos.dominio.veiculo.RepositorioVeiculo;
import com.tinnova.veiculos.dominio.veiculo.Veiculo;
import com.tinnova.veiculos.dominio.veiculo.VeiculoNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ServicoVeiculo")
class ServicoVeiculoTest {

    @Mock
    private RepositorioVeiculo repositorio;

    @InjectMocks
    private ServicoVeiculo servico;

    private Veiculo veiculoExemplo;

    @BeforeEach
    void setUp() {
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
    }

    @Nested
    @DisplayName("criar()")
    class Criar {

        @Test
        @DisplayName("deve criar veiculo com sucesso")
        void deveCriarVeiculoComSucesso() {
            // given
            when(repositorio.existsByPlaca("ABC1234")).thenReturn(false);
            when(repositorio.save(any(Veiculo.class))).thenReturn(veiculoExemplo);

            // when
            Veiculo resultado = servico.criar(veiculoExemplo);

            // then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getPlaca()).isEqualTo("ABC1234");
            verify(repositorio).save(veiculoExemplo);
        }

        @Test
        @DisplayName("deve lancar excecao quando placa duplicada")
        void deveLancarExcecaoQuandoPlacaDuplicada() {
            // given
            when(repositorio.existsByPlaca("ABC1234")).thenReturn(true);

            // when/then
            assertThatThrownBy(() -> servico.criar(veiculoExemplo))
                    .isInstanceOf(PlacaDuplicadaException.class)
                    .hasMessageContaining("ABC1234");

            verify(repositorio, never()).save(any());
        }
    }

    @Nested
    @DisplayName("atualizar()")
    class Atualizar {

        @Test
        @DisplayName("deve atualizar veiculo com sucesso")
        void deveAtualizarVeiculoComSucesso() {
            // given
            Veiculo dadosAtualizados = Veiculo.builder()
                    .placa("ABC1234")
                    .marca("Toyota")
                    .modelo("Corolla XEi")
                    .ano(2024)
                    .cor("Prata")
                    .precoUsd(new BigDecimal("28000"))
                    .build();

            when(repositorio.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(veiculoExemplo));
            when(repositorio.existsByPlacaAndIdNot("ABC1234", 1L)).thenReturn(false);
            when(repositorio.save(any(Veiculo.class))).thenReturn(veiculoExemplo);

            // when
            Veiculo resultado = servico.atualizar(1L, dadosAtualizados);

            // then
            assertThat(resultado).isNotNull();
            verify(repositorio).save(any(Veiculo.class));
        }

        @Test
        @DisplayName("deve lancar excecao quando veiculo nao encontrado")
        void deveLancarExcecaoQuandoVeiculoNaoEncontrado() {
            // given
            when(repositorio.findByIdAndAtivoTrue(99L)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> servico.atualizar(99L, veiculoExemplo))
                    .isInstanceOf(VeiculoNaoEncontradoException.class);
        }

        @Test
        @DisplayName("deve lancar excecao quando placa duplicada em atualizacao")
        void deveLancarExcecaoQuandoPlacaDuplicadaEmAtualizacao() {
            // given
            Veiculo dadosAtualizados = Veiculo.builder()
                    .placa("XYZ9999")
                    .marca("Honda")
                    .modelo("Civic")
                    .ano(2022)
                    .cor("Branco")
                    .precoUsd(new BigDecimal("22000"))
                    .build();

            when(repositorio.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(veiculoExemplo));
            when(repositorio.existsByPlacaAndIdNot("XYZ9999", 1L)).thenReturn(true);

            // when/then
            assertThatThrownBy(() -> servico.atualizar(1L, dadosAtualizados))
                    .isInstanceOf(PlacaDuplicadaException.class);
        }
    }

    @Nested
    @DisplayName("atualizarParcial()")
    class AtualizarParcial {

        @Test
        @DisplayName("deve atualizar apenas campos informados")
        void deveAtualizarApenasCamposInformados() {
            // given
            Veiculo dadosParciais = Veiculo.builder()
                    .cor("Vermelho")
                    .build();

            when(repositorio.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(veiculoExemplo));
            when(repositorio.save(any(Veiculo.class))).thenReturn(veiculoExemplo);

            // when
            servico.atualizarParcial(1L, dadosParciais);

            // then
            assertThat(veiculoExemplo.getCor()).isEqualTo("Vermelho");
            assertThat(veiculoExemplo.getMarca()).isEqualTo("Toyota");
        }

        @Test
        @DisplayName("deve validar placa duplicada em atualizacao parcial")
        void deveValidarPlacaDuplicadaEmAtualizacaoParcial() {
            // given
            Veiculo dadosParciais = Veiculo.builder()
                    .placa("XYZ9999")
                    .build();

            when(repositorio.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(veiculoExemplo));
            when(repositorio.existsByPlacaAndIdNot("XYZ9999", 1L)).thenReturn(true);

            // when/then
            assertThatThrownBy(() -> servico.atualizarParcial(1L, dadosParciais))
                    .isInstanceOf(PlacaDuplicadaException.class);
        }
    }

    @Nested
    @DisplayName("remover() - Soft Delete")
    class Remover {

        @Test
        @DisplayName("deve fazer soft delete do veiculo")
        void deveFazerSoftDeleteDoVeiculo() {
            // given
            when(repositorio.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(veiculoExemplo));
            when(repositorio.save(any(Veiculo.class))).thenReturn(veiculoExemplo);

            // when
            servico.remover(1L);

            // then
            assertThat(veiculoExemplo.getAtivo()).isFalse();
            verify(repositorio).save(veiculoExemplo);
        }

        @Test
        @DisplayName("deve lancar excecao quando veiculo nao encontrado para remocao")
        void deveLancarExcecaoQuandoVeiculoNaoEncontradoParaRemocao() {
            // given
            when(repositorio.findByIdAndAtivoTrue(99L)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> servico.remover(99L))
                    .isInstanceOf(VeiculoNaoEncontradoException.class);
        }
    }

    @Nested
    @DisplayName("listar() - Filtros")
    class Listar {

        @Test
        @DisplayName("deve listar veiculos sem filtros")
        void deveListarVeiculosSemFiltros() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Veiculo> pagina = new PageImpl<>(List.of(veiculoExemplo));
            when(repositorio.findAll(any(Specification.class), eq(pageable))).thenReturn(pagina);

            // when
            Page<Veiculo> resultado = servico.listar(null, null, null, null, null, pageable);

            // then
            assertThat(resultado.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("deve listar veiculos com filtro de marca")
        void deveListarVeiculosComFiltroDeMarca() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Veiculo> pagina = new PageImpl<>(List.of(veiculoExemplo));
            when(repositorio.findAll(any(Specification.class), eq(pageable))).thenReturn(pagina);

            // when
            Page<Veiculo> resultado = servico.listar("Toyota", null, null, null, null, pageable);

            // then
            assertThat(resultado.getContent()).hasSize(1);
            verify(repositorio).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("deve listar veiculos com filtros combinados")
        void deveListarVeiculosComFiltrosCombinados() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Veiculo> pagina = new PageImpl<>(List.of(veiculoExemplo));
            when(repositorio.findAll(any(Specification.class), eq(pageable))).thenReturn(pagina);

            // when
            Page<Veiculo> resultado = servico.listar(
                    "Toyota",
                    2023,
                    "Preto",
                    new BigDecimal("20000"),
                    new BigDecimal("30000"),
                    pageable);

            // then
            assertThat(resultado.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("buscarPorId()")
    class BuscarPorId {

        @Test
        @DisplayName("deve retornar veiculo quando encontrado")
        void deveRetornarVeiculoQuandoEncontrado() {
            // given
            when(repositorio.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(veiculoExemplo));

            // when
            Optional<Veiculo> resultado = servico.buscarPorId(1L);

            // then
            assertThat(resultado).isPresent();
            assertThat(resultado.get().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("deve retornar vazio quando nao encontrado")
        void deveRetornarVazioQuandoNaoEncontrado() {
            // given
            when(repositorio.findByIdAndAtivoTrue(99L)).thenReturn(Optional.empty());

            // when
            Optional<Veiculo> resultado = servico.buscarPorId(99L);

            // then
            assertThat(resultado).isEmpty();
        }
    }

    @Nested
    @DisplayName("contarPorMarca()")
    class ContarPorMarca {

        @Test
        @DisplayName("deve retornar contagem por marca")
        void deveRetornarContagemPorMarca() {
            // given
            List<Object[]> contagem = List.of(
                    new Object[] { "Toyota", 2L },
                    new Object[] { "Honda", 1L });
            when(repositorio.contarPorMarca()).thenReturn(contagem);

            // when
            List<Object[]> resultado = servico.contarPorMarca();

            // then
            assertThat(resultado).hasSize(2);
        }
    }
}
