package com.tinnova.veiculos.api.excecao;

import com.tinnova.veiculos.api.dto.ErroResponse;
import com.tinnova.veiculos.dominio.veiculo.PlacaDuplicadaException;
import com.tinnova.veiculos.dominio.veiculo.VeiculoNaoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Handler centralizado de exceções da API.
 */
@RestControllerAdvice
public class ManipuladorGlobalExcecoes {

    @ExceptionHandler(VeiculoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> handleNaoEncontrado(VeiculoNaoEncontradoException ex, HttpServletRequest request) {
        ErroResponse erro = ErroResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .erro("Not Found")
                .mensagem(ex.getMessage())
                .caminho(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(PlacaDuplicadaException.class)
    public ResponseEntity<ErroResponse> handlePlacaDuplicada(PlacaDuplicadaException ex, HttpServletRequest request) {
        ErroResponse erro = ErroResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .erro("Conflict")
                .mensagem(ex.getMessage())
                .caminho(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> handleValidacao(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ErroResponse.CampoErro> detalhes = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new ErroResponse.CampoErro(fieldError.getField(), fieldError.getDefaultMessage()))
                .toList();

        ErroResponse erro = ErroResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .erro("Bad Request")
                .mensagem("Erro de validação")
                .caminho(request.getRequestURI())
                .detalhes(detalhes)
                .build();

        return ResponseEntity.badRequest().body(erro);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> handleGenerico(Exception ex, HttpServletRequest request) {
        ErroResponse erro = ErroResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .erro("Internal Server Error")
                .mensagem("Erro interno do servidor")
                .caminho(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
    }
}
