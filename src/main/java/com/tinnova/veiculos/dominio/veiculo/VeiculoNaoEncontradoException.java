package com.tinnova.veiculos.dominio.veiculo;

/**
 * Exceção lançada quando um veículo não é encontrado.
 */
public class VeiculoNaoEncontradoException extends RuntimeException {

    /**
     * @param id identificador do veículo não encontrado
     */
    public VeiculoNaoEncontradoException(Long id) {
        super("Veículo não encontrado: " + id);
    }
}
