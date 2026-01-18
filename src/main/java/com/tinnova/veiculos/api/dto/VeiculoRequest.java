package com.tinnova.veiculos.api.dto;

import com.tinnova.veiculos.dominio.veiculo.Veiculo;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO para criação e atualização de veículos.
 */
@Data
public class VeiculoRequest {

    @NotBlank(message = "Placa é obrigatória")
    @Size(min = 7, max = 10, message = "Placa deve ter entre 7 e 10 caracteres")
    private String placa;

    @NotBlank(message = "Marca é obrigatória")
    @Size(max = 50, message = "Marca deve ter no máximo 50 caracteres")
    private String marca;

    @NotBlank(message = "Modelo é obrigatório")
    @Size(max = 100, message = "Modelo deve ter no máximo 100 caracteres")
    private String modelo;

    @NotNull(message = "Ano é obrigatório")
    @Min(value = 1900, message = "Ano deve ser maior que 1900")
    @Max(value = 2100, message = "Ano deve ser menor que 2100")
    private Integer ano;

    @NotBlank(message = "Cor é obrigatória")
    @Size(max = 30, message = "Cor deve ter no máximo 30 caracteres")
    private String cor;

    @NotNull(message = "Preço é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
    private BigDecimal precoUsd;

    /**
     * Converte para entidade Veiculo.
     */
    public Veiculo toEntity() {
        return Veiculo.builder()
                .placa(this.placa)
                .marca(this.marca)
                .modelo(this.modelo)
                .ano(this.ano)
                .cor(this.cor)
                .precoUsd(this.precoUsd)
                .build();
    }
}
