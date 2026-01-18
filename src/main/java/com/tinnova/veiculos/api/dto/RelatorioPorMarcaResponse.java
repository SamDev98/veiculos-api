package com.tinnova.veiculos.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO para relatório de contagem por marca.
 */
@Data
@AllArgsConstructor
public class RelatorioPorMarcaResponse {

    private String marca;
    private Long quantidade;
}
