package com.tinnova.veiculos.api.controller;

import com.tinnova.veiculos.api.dto.LoginRequest;
import com.tinnova.veiculos.api.dto.LoginResponse;
import com.tinnova.veiculos.infraestrutura.seguranca.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Controller de autenticação.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;

    private static final Map<String, UsuarioMemoria> USUARIOS = Map.of(
            "admin", new UsuarioMemoria("admin", "admin", List.of("ROLE_ADMIN", "ROLE_USER")),
            "user", new UsuarioMemoria("user", "user", List.of("ROLE_USER")));

    /**
     * Autentica usuário e retorna token JWT.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        UsuarioMemoria usuario = USUARIOS.get(request.getUsuario());

        if (usuario == null || !usuario.senha().equals(request.getSenha())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = jwtUtil.gerarToken(usuario.username(), usuario.roles());
        return ResponseEntity.ok(LoginResponse.of(token, usuario.username(), usuario.roles()));
    }

    record UsuarioMemoria(String username, String senha, List<String> roles) {
    }
}
