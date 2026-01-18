package com.tinnova.veiculos.dominio.veiculo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioVeiculo extends JpaRepository<Veiculo, Long>, JpaSpecificationExecutor<Veiculo> {

    List<Veiculo> findByAtivoTrue();

    Optional<Veiculo> findByIdAndAtivoTrue(Long id);

    boolean existsByPlaca(String placa);

    boolean existsByPlacaAndIdNot(String placa, Long id);

    @Query("SELECT v.marca, COUNT(v) FROM Veiculo v WHERE v.ativo = true GROUP BY v.marca")
    List<Object[]> contarPorMarca();

}
