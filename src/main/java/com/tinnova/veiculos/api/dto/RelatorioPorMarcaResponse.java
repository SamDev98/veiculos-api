package com.tinnova.veiculos.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RelatorioPorMarcaResponse {

    private String marca;
    private Long quantidade;
}
