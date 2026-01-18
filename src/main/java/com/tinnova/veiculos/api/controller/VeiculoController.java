package com.tinnova.veiculos.api.controller;

import com.tinnova.veiculos.api.dto.RelatorioPorMarcaResponse;
import com.tinnova.veiculos.api.dto.VeiculoRequest;
import com.tinnova.veiculos.api.dto.VeiculoResponse;
import com.tinnova.veiculos.aplicacao.veiculo.ServicoVeiculo;
import com.tinnova.veiculos.dominio.veiculo.VeiculoNaoEncontradoException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller REST para operações de veículos.
 */
@RestController
@RequestMapping("/veiculos")
@RequiredArgsConstructor
public class VeiculoController {

    private final ServicoVeiculo servicoVeiculo;

    /**
     * Lista veículos com filtros e paginação.
     */
    @GetMapping
    public ResponseEntity<Page<VeiculoResponse>> listar(
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) String cor,
            @RequestParam(required = false) BigDecimal minPreco,
            @RequestParam(required = false) BigDecimal maxPreco,
            Pageable pageable) {

        Page<VeiculoResponse> response = servicoVeiculo
                .listar(marca, ano, cor, minPreco, maxPreco, pageable)
                .map(VeiculoResponse::fromEntity);

        return ResponseEntity.ok(response);
    }

    /**
     * Busca veículo por ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<VeiculoResponse> buscarPorId(@PathVariable Long id) {
        return servicoVeiculo.buscarPorId(id)
                .map(VeiculoResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new VeiculoNaoEncontradoException(id));
    }

    /**
     * Retorna contagem de veículos por marca.
     */
    @GetMapping("/relatorios/por-marca")
    public ResponseEntity<List<RelatorioPorMarcaResponse>> relatorioPorMarca() {
        List<RelatorioPorMarcaResponse> relatorio = servicoVeiculo.contarPorMarca().stream()
                .map(row -> new RelatorioPorMarcaResponse((String) row[0], (Long) row[1]))
                .toList();

        return ResponseEntity.ok(relatorio);
    }

    /**
     * Cria um novo veículo. Requer ADMIN.
     */
    @PostMapping
    public ResponseEntity<VeiculoResponse> criar(@Valid @RequestBody VeiculoRequest request) {
        var veiculo = servicoVeiculo.criar(request.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(VeiculoResponse.fromEntity(veiculo));
    }

    /**
     * Atualiza todos os campos de um veículo. Requer ADMIN.
     */
    @PutMapping("/{id}")
    public ResponseEntity<VeiculoResponse> atualizar(@PathVariable Long id,
            @Valid @RequestBody VeiculoRequest request) {
        var veiculo = servicoVeiculo.atualizar(id, request.toEntity());
        return ResponseEntity.ok(VeiculoResponse.fromEntity(veiculo));
    }

    /**
     * Atualiza campos específicos de um veículo. Requer ADMIN.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<VeiculoResponse> atualizarParcial(@PathVariable Long id,
            @RequestBody VeiculoRequest request) {
        var veiculo = servicoVeiculo.atualizarParcial(id, request.toEntity());
        return ResponseEntity.ok(VeiculoResponse.fromEntity(veiculo));
    }

    /**
     * Remove um veículo (soft delete). Requer ADMIN.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        servicoVeiculo.remover(id);
        return ResponseEntity.noContent().build();
    }
}
