package com.tinnova.veiculos.dominio.veiculo;

/**
 * Exceção lançada quando uma placa já existe no sistema.
 */
public class PlacaDuplicadaException extends RuntimeException {

    /**
     * @param placa placa duplicada
     */
    public PlacaDuplicadaException(String placa) {
        super("Placa já cadastrada: " + placa);
    }
}
