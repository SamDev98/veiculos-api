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

@RestController
@RequestMapping("/veiculos")
@RequiredArgsConstructor
public class VeiculoController {

    private final ServicoVeiculo servicoVeiculo;

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

    @GetMapping("/{id}")
    public ResponseEntity<VeiculoResponse> buscarPorId(@PathVariable Long id) {
        return servicoVeiculo.buscarPorId(id)
                .map(VeiculoResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new VeiculoNaoEncontradoException(id));
    }

    @GetMapping("/relatorios/por-marca")
    public ResponseEntity<List<RelatorioPorMarcaResponse>> relatorioPorMarca() {
        List<RelatorioPorMarcaResponse> relatorio = servicoVeiculo.contarPorMarca().stream()
                .map(row -> new RelatorioPorMarcaResponse((String) row[0], (Long) row[1]))
                .toList();

        return ResponseEntity.ok(relatorio);
    }

    @PostMapping
    public ResponseEntity<VeiculoResponse> criar(@Valid @RequestBody VeiculoRequest request) {
        var veiculo = servicoVeiculo.criar(request.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(VeiculoResponse.fromEntity(veiculo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VeiculoResponse> atualizar(@PathVariable Long id,
            @Valid @RequestBody VeiculoRequest request) {
        var veiculo = servicoVeiculo.atualizar(id, request.toEntity());
        return ResponseEntity.ok(VeiculoResponse.fromEntity(veiculo));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<VeiculoResponse> atualizarParcial(@PathVariable Long id,
            @RequestBody VeiculoRequest request) {
        var veiculo = servicoVeiculo.atualizarParcial(id, request.toEntity());
        return ResponseEntity.ok(VeiculoResponse.fromEntity(veiculo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        servicoVeiculo.remover(id);
        return ResponseEntity.noContent().build();
    }
}
