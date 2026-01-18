package com.tinnova.veiculos.api.dto;

import com.tinnova.veiculos.dominio.veiculo.Veiculo;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class VeiculoResponse {

    private Long id;
    private String placa;
    private String marca;
    private String modelo;
    private Integer ano;
    private String cor;
    private BigDecimal precoUsd;
    private BigDecimal precoBrl;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public static VeiculoResponse fromEntity(Veiculo veiculo) {
        return VeiculoResponse.builder()
                .id(veiculo.getId())
                .placa(veiculo.getPlaca())
                .marca(veiculo.getMarca())
                .modelo(veiculo.getModelo())
                .ano(veiculo.getAno())
                .cor(veiculo.getCor())
                .precoUsd(veiculo.getPrecoUsd())
                .criadoEm(veiculo.getCriadoEm())
                .atualizadoEm(veiculo.getAtualizadoEm())
                .build();
    }
}
