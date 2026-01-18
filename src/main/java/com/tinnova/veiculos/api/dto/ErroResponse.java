package com.tinnova.veiculos.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO padrão para respostas de erro da API.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErroResponse {

    private LocalDateTime timestamp;
    private Integer status;
    private String erro;
    private String mensagem;
    private String caminho;
    private List<CampoErro> detalhes;

    /**
     * Detalhes de erro por campo (validação).
     */
    @Data
    @AllArgsConstructor
    public static class CampoErro {
        private String campo;
        private String mensagem;
    }
}
