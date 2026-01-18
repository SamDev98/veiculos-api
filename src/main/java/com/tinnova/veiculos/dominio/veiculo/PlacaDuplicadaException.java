package com.tinnova.veiculos.dominio.veiculo;

public class PlacaDuplicadaException extends RuntimeException {
  public PlacaDuplicadaException(String placa) {
    super("Placa já cadastrada: " + placa);
  }
}
