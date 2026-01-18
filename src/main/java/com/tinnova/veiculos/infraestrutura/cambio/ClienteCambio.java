package com.tinnova.veiculos.infraestrutura.cambio;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Interface para clientes de APIs de cotação de câmbio.
 */
public interface ClienteCambio {

    /**
     * Obtém a cotação USD/BRL.
     *
     * @return cotação ou vazio se falhar
     */
    Optional<BigDecimal> obterCotacaoUsdBrl();
}
