package com.tinnova.veiculos.aplicacao.veiculo;

import com.tinnova.veiculos.dominio.veiculo.PlacaDuplicadaException;
import com.tinnova.veiculos.dominio.veiculo.RepositorioVeiculo;
import com.tinnova.veiculos.dominio.veiculo.Veiculo;
import com.tinnova.veiculos.dominio.veiculo.VeiculoNaoEncontradoException;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Serviço de aplicação para operações de veículos.
 */
@Service
@RequiredArgsConstructor
public class ServicoVeiculo {

    private final RepositorioVeiculo repositorio;

    /**
     * Lista veículos ativos com filtros opcionais e paginação.
     */
    @Transactional(readOnly = true)
    public Page<Veiculo> listar(String marca, Integer ano, String cor,
            BigDecimal minPreco, BigDecimal maxPreco,
            Pageable pageable) {
        Specification<Veiculo> spec = Specification.where(ativoTrue());

        if (marca != null && !marca.isBlank()) {
            spec = spec.and(marcaEquals(marca));
        }
        if (ano != null) {
            spec = spec.and(anoEquals(ano));
        }
        if (cor != null && !cor.isBlank()) {
            spec = spec.and(corEquals(cor));
        }
        if (minPreco != null) {
            spec = spec.and(precoMaiorOuIgual(minPreco));
        }
        if (maxPreco != null) {
            spec = spec.and(precoMenorOuIgual(maxPreco));
        }

        return repositorio.findAll(spec, pageable);
    }

    /**
     * Busca veículo ativo por ID.
     */
    @Transactional(readOnly = true)
    public Optional<Veiculo> buscarPorId(Long id) {
        return repositorio.findByIdAndAtivoTrue(id);
    }

    /**
     * Cria um novo veículo. Lança PlacaDuplicadaException se a placa já existir.
     */
    @Transactional
    public Veiculo criar(Veiculo veiculo) {
        if (repositorio.existsByPlaca(veiculo.getPlaca())) {
            throw new PlacaDuplicadaException(veiculo.getPlaca());
        }
        return repositorio.save(veiculo);
    }

    /**
     * Atualiza todos os campos de um veículo (PUT).
     */
    @Transactional
    public Veiculo atualizar(Long id, Veiculo dadosAtualizados) {
        Veiculo existente = repositorio.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new VeiculoNaoEncontradoException(id));

        if (repositorio.existsByPlacaAndIdNot(dadosAtualizados.getPlaca(), id)) {
            throw new PlacaDuplicadaException(dadosAtualizados.getPlaca());
        }

        existente.setPlaca(dadosAtualizados.getPlaca());
        existente.setMarca(dadosAtualizados.getMarca());
        existente.setModelo(dadosAtualizados.getModelo());
        existente.setAno(dadosAtualizados.getAno());
        existente.setCor(dadosAtualizados.getCor());
        existente.setPrecoUsd(dadosAtualizados.getPrecoUsd());

        return repositorio.save(existente);
    }

    /**
     * Atualiza apenas os campos informados (PATCH).
     */
    @Transactional
    public Veiculo atualizarParcial(Long id, Veiculo dadosParciais) {
        Veiculo existente = repositorio.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new VeiculoNaoEncontradoException(id));

        if (dadosParciais.getPlaca() != null) {
            if (repositorio.existsByPlacaAndIdNot(dadosParciais.getPlaca(), id)) {
                throw new PlacaDuplicadaException(dadosParciais.getPlaca());
            }
            existente.setPlaca(dadosParciais.getPlaca());
        }
        if (dadosParciais.getMarca() != null) {
            existente.setMarca(dadosParciais.getMarca());
        }
        if (dadosParciais.getModelo() != null) {
            existente.setModelo(dadosParciais.getModelo());
        }
        if (dadosParciais.getAno() != null) {
            existente.setAno(dadosParciais.getAno());
        }
        if (dadosParciais.getCor() != null) {
            existente.setCor(dadosParciais.getCor());
        }
        if (dadosParciais.getPrecoUsd() != null) {
            existente.setPrecoUsd(dadosParciais.getPrecoUsd());
        }

        return repositorio.save(existente);
    }

    /**
     * Remove um veículo (soft delete).
     */
    @Transactional
    public void remover(Long id) {
        Veiculo veiculo = repositorio.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new VeiculoNaoEncontradoException(id));
        veiculo.desativar();
        repositorio.save(veiculo);
    }

    /**
     * Conta veículos ativos agrupados por marca.
     */
    @Transactional(readOnly = true)
    public List<Object[]> contarPorMarca() {
        return repositorio.contarPorMarca();
    }

    private Specification<Veiculo> ativoTrue() {
        return (root, query, cb) -> cb.isTrue(root.get("ativo"));
    }

    private Specification<Veiculo> marcaEquals(String marca) {
        return (root, query, cb) -> cb.equal(cb.lower(root.get("marca")), marca.toLowerCase());
    }

    private Specification<Veiculo> anoEquals(Integer ano) {
        return (root, query, cb) -> cb.equal(root.get("ano"), ano);
    }

    private Specification<Veiculo> corEquals(String cor) {
        return (root, query, cb) -> cb.equal(cb.lower(root.get("cor")), cor.toLowerCase());
    }

    private Specification<Veiculo> precoMaiorOuIgual(BigDecimal min) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("precoUsd"), min);
    }

    private Specification<Veiculo> precoMenorOuIgual(BigDecimal max) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("precoUsd"), max);
    }
}
