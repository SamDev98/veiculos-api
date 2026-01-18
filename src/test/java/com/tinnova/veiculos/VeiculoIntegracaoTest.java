package com.tinnova.veiculos;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinnova.veiculos.api.dto.LoginRequest;
import com.tinnova.veiculos.api.dto.VeiculoRequest;
import com.tinnova.veiculos.dominio.veiculo.RepositorioVeiculo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Testes de Integração - Veículos")
class VeiculoIntegracaoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RepositorioVeiculo repositorio;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String tokenAdmin;
    private String tokenUser;

    @BeforeEach
    void setUp() throws Exception {
        repositorio.deleteAll();
        tokenAdmin = obterToken("admin", "admin");
        tokenUser = obterToken("user", "user");
    }

    private String obterToken(String usuario, String senha) throws Exception {
        LoginRequest login = new LoginRequest();
        login.setUsuario(usuario);
        login.setSenha(senha);

        MvcResult resultado = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        String resposta = resultado.getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("token").asText();
    }

    @Nested
    @DisplayName("Fluxo completo - Happy Path")
    class FluxoCompleto {

        @Test
        @DisplayName("deve executar fluxo: login → criar → listar → buscar por id")
        void deveExecutarFluxoCompleto() throws Exception {
            // 1. Criar veículo (ADMIN)
            VeiculoRequest request = criarVeiculoRequest("ABC1234", "Toyota", "Corolla");

            MvcResult resultadoCriacao = mockMvc.perform(post("/veiculos")
                            .header("Authorization", "Bearer " + tokenAdmin)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.placa").value("ABC1234"))
                    .andExpect(jsonPath("$.marca").value("Toyota"))
                    .andReturn();

            Long idCriado = objectMapper.readTree(resultadoCriacao.getResponse().getContentAsString())
                    .get("id").asLong();

            // 2. Listar veículos
            mockMvc.perform(get("/veiculos")
                            .header("Authorization", "Bearer " + tokenAdmin))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].placa").value("ABC1234"));

            // 3. Buscar por ID
            mockMvc.perform(get("/veiculos/{id}", idCriado)
                            .header("Authorization", "Bearer " + tokenAdmin))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(idCriado))
                    .andExpect(jsonPath("$.placa").value("ABC1234"));
        }
    }

    @Nested
    @DisplayName("Segurança - 401 Não Autenticado")
    class Seguranca401 {

        @Test
        @DisplayName("deve retornar 401 para GET /veiculos sem token")
        void deveRetornar401ParaListarSemToken() throws Exception {
            mockMvc.perform(get("/veiculos"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("deve retornar 401 para POST /veiculos sem token")
        void deveRetornar401ParaCriarSemToken() throws Exception {
            VeiculoRequest request = criarVeiculoRequest("ABC1234", "Toyota", "Corolla");

            mockMvc.perform(post("/veiculos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("deve retornar 401 para token inválido")
        void deveRetornar401ParaTokenInvalido() throws Exception {
            mockMvc.perform(get("/veiculos")
                            .header("Authorization", "Bearer token-invalido"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Segurança - 403 Sem Permissão")
    class Seguranca403 {

        @Test
        @DisplayName("deve retornar 403 para USER tentando POST")
        void deveRetornar403ParaUserTentandoPost() throws Exception {
            VeiculoRequest request = criarVeiculoRequest("ABC1234", "Toyota", "Corolla");

            mockMvc.perform(post("/veiculos")
                            .header("Authorization", "Bearer " + tokenUser)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("deve retornar 403 para USER tentando PUT")
        void deveRetornar403ParaUserTentandoPut() throws Exception {
            VeiculoRequest request = criarVeiculoRequest("ABC1234", "Toyota", "Corolla");

            mockMvc.perform(put("/veiculos/{id}", 1L)
                            .header("Authorization", "Bearer " + tokenUser)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("deve retornar 403 para USER tentando PATCH")
        void deveRetornar403ParaUserTentandoPatch() throws Exception {
            VeiculoRequest request = criarVeiculoRequest("ABC1234", "Toyota", "Corolla");

            mockMvc.perform(patch("/veiculos/{id}", 1L)
                            .header("Authorization", "Bearer " + tokenUser)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("deve retornar 403 para USER tentando DELETE")
        void deveRetornar403ParaUserTentandoDelete() throws Exception {
            mockMvc.perform(delete("/veiculos/{id}", 1L)
                            .header("Authorization", "Bearer " + tokenUser))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("deve permitir USER fazer GET")
        void devePermitirUserFazerGet() throws Exception {
            mockMvc.perform(get("/veiculos")
                            .header("Authorization", "Bearer " + tokenUser))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Conflito - 409 Placa Duplicada")
    class Conflito409 {

        @Test
        @DisplayName("deve retornar 409 ao criar veículo com placa duplicada")
        void deveRetornar409AoCriarComPlacaDuplicada() throws Exception {
            VeiculoRequest request = criarVeiculoRequest("DUP1234", "Toyota", "Corolla");

            // Criar primeiro veículo
            mockMvc.perform(post("/veiculos")
                            .header("Authorization", "Bearer " + tokenAdmin)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            // Tentar criar segundo com mesma placa
            VeiculoRequest duplicada = criarVeiculoRequest("DUP1234", "Honda", "Civic");

            mockMvc.perform(post("/veiculos")
                            .header("Authorization", "Bearer " + tokenAdmin)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(duplicada)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.erro").value("Conflict"));
        }

        @Test
        @DisplayName("deve retornar 409 ao atualizar para placa existente")
        void deveRetornar409AoAtualizarParaPlacaExistente() throws Exception {
            // Criar primeiro veículo
            VeiculoRequest request1 = criarVeiculoRequest("PLA0001", "Toyota", "Corolla");
            mockMvc.perform(post("/veiculos")
                            .header("Authorization", "Bearer " + tokenAdmin)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request1)))
                    .andExpect(status().isCreated());

            // Criar segundo veículo
            VeiculoRequest request2 = criarVeiculoRequest("PLA0002", "Honda", "Civic");
            MvcResult resultado = mockMvc.perform(post("/veiculos")
                            .header("Authorization", "Bearer " + tokenAdmin)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request2)))
                    .andExpect(status().isCreated())
                    .andReturn();

            Long idSegundo = objectMapper.readTree(resultado.getResponse().getContentAsString())
                    .get("id").asLong();

            // Tentar atualizar segundo para placa do primeiro
            VeiculoRequest atualizacao = criarVeiculoRequest("PLA0001", "Honda", "Civic");

            mockMvc.perform(put("/veiculos/{id}", idSegundo)
                            .header("Authorization", "Bearer " + tokenAdmin)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(atualizacao)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("Validação - 400 Bad Request")
    class Validacao400 {

        @Test
        @DisplayName("deve retornar 400 para campos obrigatórios vazios")
        void deveRetornar400ParaCamposObrigatoriosVazios() throws Exception {
            VeiculoRequest request = new VeiculoRequest();

            mockMvc.perform(post("/veiculos")
                            .header("Authorization", "Bearer " + tokenAdmin)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.erro").value("Bad Request"))
                    .andExpect(jsonPath("$.detalhes").isArray());
        }

        @Test
        @DisplayName("deve retornar 400 para ano inválido")
        void deveRetornar400ParaAnoInvalido() throws Exception {
            VeiculoRequest request = criarVeiculoRequest("VAL1234", "Toyota", "Corolla");
            request.setAno(1800); // ano inválido

            mockMvc.perform(post("/veiculos")
                            .header("Authorization", "Bearer " + tokenAdmin)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Not Found - 404")
    class NotFound404 {

        @Test
        @DisplayName("deve retornar 404 para veículo inexistente")
        void deveRetornar404ParaVeiculoInexistente() throws Exception {
            mockMvc.perform(get("/veiculos/{id}", 99999L)
                            .header("Authorization", "Bearer " + tokenAdmin))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.erro").value("Not Found"));
        }
    }

    @Nested
    @DisplayName("CRUD Completo - ADMIN")
    class CrudCompleto {

        @Test
        @DisplayName("deve executar CRUD completo: criar → atualizar → patch → deletar")
        void deveExecutarCrudCompleto() throws Exception {
            // CREATE
            VeiculoRequest request = criarVeiculoRequest("CRD1234", "Toyota", "Corolla");

            MvcResult criado = mockMvc.perform(post("/veiculos")
                            .header("Authorization", "Bearer " + tokenAdmin)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andReturn();

            Long id = objectMapper.readTree(criado.getResponse().getContentAsString())
                    .get("id").asLong();

            // UPDATE (PUT)
            VeiculoRequest atualizado = criarVeiculoRequest("CRD1234", "Honda", "Civic");

            mockMvc.perform(put("/veiculos/{id}", id)
                            .header("Authorization", "Bearer " + tokenAdmin)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(atualizado)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.marca").value("Honda"))
                    .andExpect(jsonPath("$.modelo").value("Civic"));

            // PATCH
            VeiculoRequest parcial = new VeiculoRequest();
            parcial.setCor("Vermelho");

            mockMvc.perform(patch("/veiculos/{id}", id)
                            .header("Authorization", "Bearer " + tokenAdmin)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(parcial)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cor").value("Vermelho"));

            // DELETE (soft delete)
            mockMvc.perform(delete("/veiculos/{id}", id)
                            .header("Authorization", "Bearer " + tokenAdmin))
                    .andExpect(status().isNoContent());

            // Verificar que não aparece mais na listagem
            mockMvc.perform(get("/veiculos/{id}", id)
                            .header("Authorization", "Bearer " + tokenAdmin))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Filtros")
    class Filtros {

        @Test
        @DisplayName("deve filtrar por marca")
        void deveFiltrarPorMarca() throws Exception {
            // Criar veículos de marcas diferentes
            criarVeiculoViaApi("FLT0001", "Toyota", "Corolla");
            criarVeiculoViaApi("FLT0002", "Honda", "Civic");
            criarVeiculoViaApi("FLT0003", "Toyota", "Hilux");

            mockMvc.perform(get("/veiculos")
                            .param("marca", "Toyota")
                            .header("Authorization", "Bearer " + tokenAdmin))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2));
        }

        @Test
        @DisplayName("deve filtrar por faixa de preço")
        void deveFiltrarPorFaixaDePreco() throws Exception {
            criarVeiculoViaApiComPreco("PRE0001", "Toyota", "Corolla", new BigDecimal("20000"));
            criarVeiculoViaApiComPreco("PRE0002", "Honda", "Civic", new BigDecimal("30000"));
            criarVeiculoViaApiComPreco("PRE0003", "Ford", "Focus", new BigDecimal("40000"));

            mockMvc.perform(get("/veiculos")
                            .param("minPreco", "25000")
                            .param("maxPreco", "35000")
                            .header("Authorization", "Bearer " + tokenAdmin))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].marca").value("Honda"));
        }
    }

    @Nested
    @DisplayName("Relatório por Marca")
    class RelatorioPorMarca {

        @Test
        @DisplayName("deve retornar relatório agrupado por marca")
        void deveRetornarRelatorioAgrupadoPorMarca() throws Exception {
            criarVeiculoViaApi("REL0001", "Toyota", "Corolla");
            criarVeiculoViaApi("REL0002", "Toyota", "Hilux");
            criarVeiculoViaApi("REL0003", "Honda", "Civic");

            mockMvc.perform(get("/veiculos/relatorios/por-marca")
                            .header("Authorization", "Bearer " + tokenAdmin))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2));
        }
    }

    // Métodos auxiliares

    private VeiculoRequest criarVeiculoRequest(String placa, String marca, String modelo) {
        VeiculoRequest request = new VeiculoRequest();
        request.setPlaca(placa);
        request.setMarca(marca);
        request.setModelo(modelo);
        request.setAno(2023);
        request.setCor("Preto");
        request.setPrecoUsd(new BigDecimal("25000"));
        return request;
    }

    private void criarVeiculoViaApi(String placa, String marca, String modelo) throws Exception {
        VeiculoRequest request = criarVeiculoRequest(placa, marca, modelo);
        mockMvc.perform(post("/veiculos")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    private void criarVeiculoViaApiComPreco(String placa, String marca, String modelo, BigDecimal preco) throws Exception {
        VeiculoRequest request = criarVeiculoRequest(placa, marca, modelo);
        request.setPrecoUsd(preco);
        mockMvc.perform(post("/veiculos")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
