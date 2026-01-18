package com.tinnova.veiculos.infraestrutura.cambio;

import java.math.BigDecimal;
import java.util.Optional;

public interface ClienteCambio {

    Optional<BigDecimal> obterCotacaoUsdBrl();
}
