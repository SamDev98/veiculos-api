package com.tinnova.veiculos.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String tipo;
    private String usuario;
    private List<String> roles;

    public static LoginResponse of(String token, String usuario, List<String> roles) {
        return new LoginResponse(token, "Bearer", usuario, roles);
    }
}
