package com.tinnova.veiculos.dominio.veiculo;

public class VeiculoNaoEncontradoException extends RuntimeException {
  public VeiculoNaoEncontradoException(Long id) {
    super("Veículo não encontrado: " + id);
  }
}
