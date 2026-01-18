package com.tinnova.veiculos.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * DTO de resposta de login com token JWT.
 */
@Data
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String tipo;
    private String usuario;
    private List<String> roles;

    /**
     * Cria resposta de login com tipo Bearer.
     */
    public static LoginResponse of(String token, String usuario, List<String> roles) {
        return new LoginResponse(token, "Bearer", usuario, roles);
    }
}
