package br.insper.avaliacoes.controller;

import br.insper.avaliacoes.dto.AvaliacaoRequest;
import br.insper.avaliacoes.entity.Avaliacao;
import br.insper.avaliacoes.entity.StatusAvaliacao;
import br.insper.avaliacoes.repository.AvaliacaoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class AvaliacaoControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres
            = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16"));

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AvaliacaoRepository repository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    private String json(AvaliacaoRequest request) throws Exception {
        return objectMapper.writeValueAsString(request);
    }

    private Avaliacao salvar(String avaliado, int nota) {
        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setEmailAvaliador("admin@insper.edu.br");
        avaliacao.setEmailAvaliado(avaliado);
        avaliacao.setComentarios("Comentario");
        avaliacao.setNota(nota);
        avaliacao.setDataAvaliacao(LocalDateTime.now());
        avaliacao.setStatus(StatusAvaliacao.ATIVA);
        return repository.save(avaliacao);
    }

    @Test
    void criarAvaliacao_comoAdmin_deveSalvarERetornar201() throws Exception {
        var request = new AvaliacaoRequest("avaliado@insper.edu.br", "Excelente", 9, null);

        mockMvc.perform(post("/avaliacoes")
                        .with(jwt().jwt(j -> j
                                .claim("https://social-insper.com/roles", List.of("ADMIN"))
                                .claim("https://social-insper.com/email", "admin@insper.edu.br")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.emailAvaliador").value("admin@insper.edu.br"))
                .andExpect(jsonPath("$.emailAvaliado").value("avaliado@insper.edu.br"))
                .andExpect(jsonPath("$.nota").value(9))
                .andExpect(jsonPath("$.status").value("ATIVA"));

        assertThat(repository.findAll()).hasSize(1);
        assertThat(repository.findAll().get(0).getEmailAvaliador()).isEqualTo("admin@insper.edu.br");
        assertThat(repository.findAll().get(0).getDataAvaliacao()).isNotNull();
    }

    @Test
    void criarAvaliacao_comoUser_deveRetornar403() throws Exception {
        var request = new AvaliacaoRequest("avaliado@insper.edu.br", "Excelente", 9, null);

        mockMvc.perform(post("/avaliacoes")
                        .with(jwt().jwt(j -> j
                                .claim("https://social-insper.com/roles", List.of("USER"))
                                .claim("https://social-insper.com/email", "user@insper.edu.br")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isForbidden());

        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void criarAvaliacao_semRoles_deveRetornar403() throws Exception {
        var request = new AvaliacaoRequest("avaliado@insper.edu.br", "Excelente", 9, null);

        mockMvc.perform(post("/avaliacoes")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isForbidden());

        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void criarAvaliacao_comoAdminSemEmail_deveRetornar400() throws Exception {
        var request = new AvaliacaoRequest("avaliado@insper.edu.br", "Excelente", 9, null);

        mockMvc.perform(post("/avaliacoes")
                        .with(jwt().jwt(j -> j.claim("https://social-insper.com/roles", List.of("ADMIN"))))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest());

        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void criarAvaliacao_comNota11_deveRetornar400() throws Exception {
        var request = new AvaliacaoRequest("avaliado@insper.edu.br", "Excelente", 11, null);

        mockMvc.perform(post("/avaliacoes")
                        .with(jwt().jwt(j -> j
                                .claim("https://social-insper.com/roles", List.of("ADMIN"))
                                .claim("https://social-insper.com/email", "admin@insper.edu.br")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("A nota deve estar entre 0 e 10"));

        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void criarAvaliacao_semAutenticacao_deveRetornar401() throws Exception {
        var request = new AvaliacaoRequest("avaliado@insper.edu.br", "Excelente", 9, null);

        mockMvc.perform(post("/avaliacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listarAvaliacoes_comoUser_deveRetornarLista() throws Exception {
        salvar("a@insper.edu.br", 7);
        salvar("b@insper.edu.br", 8);

        mockMvc.perform(get("/avaliacoes")
                        .with(jwt().jwt(j -> j.claim("https://social-insper.com/roles", List.of("USER")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status").value("ATIVA"));
    }

    @Test
    void listarAvaliacoes_comoAdmin_deveRetornar200() throws Exception {
        mockMvc.perform(get("/avaliacoes")
                        .with(jwt().jwt(j -> j.claim("https://social-insper.com/roles", List.of("ADMIN")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void listarAvaliacoes_semAutenticacao_deveRetornar401() throws Exception {
        mockMvc.perform(get("/avaliacoes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void criarAvaliacao_comStatusInformado_deveManterStatus() throws Exception {
        var request = new AvaliacaoRequest("avaliado@insper.edu.br", "Excelente", 9, StatusAvaliacao.ARQUIVADA);

        mockMvc.perform(post("/avaliacoes")
                        .with(jwt().jwt(j -> j
                                .claim("https://social-insper.com/roles", List.of("ADMIN"))
                                .claim("https://social-insper.com/email", "admin@insper.edu.br")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ARQUIVADA"));
    }

    @Test
    void criarAvaliacao_comStatusInvalido_deveRetornar400() throws Exception {
        String body = "{\"emailAvaliado\":\"avaliado@insper.edu.br\",\"comentarios\":\"Excelente\",\"nota\":9,\"status\":\"XPTO\"}";

        mockMvc.perform(post("/avaliacoes")
                        .with(jwt().jwt(j -> j
                                .claim("https://social-insper.com/roles", List.of("ADMIN"))
                                .claim("https://social-insper.com/email", "admin@insper.edu.br")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void excluirAvaliacao_comoAdmin_deveRetornar204ERemover() throws Exception {
        Avaliacao avaliacao = salvar("a@insper.edu.br", 7);

        mockMvc.perform(delete("/avaliacoes/" + avaliacao.getId())
                        .with(jwt().jwt(j -> j.claim("https://social-insper.com/roles", List.of("ADMIN")))))
                .andExpect(status().isNoContent());

        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void excluirAvaliacao_comoUser_deveRetornar403() throws Exception {
        Avaliacao avaliacao = salvar("a@insper.edu.br", 7);

        mockMvc.perform(delete("/avaliacoes/" + avaliacao.getId())
                        .with(jwt().jwt(j -> j.claim("https://social-insper.com/roles", List.of("USER")))))
                .andExpect(status().isForbidden());

        assertThat(repository.findAll()).hasSize(1);
    }

    @Test
    void excluirAvaliacao_semRoles_deveRetornar403() throws Exception {
        Avaliacao avaliacao = salvar("a@insper.edu.br", 7);

        mockMvc.perform(delete("/avaliacoes/" + avaliacao.getId())
                        .with(jwt()))
                .andExpect(status().isForbidden());

        assertThat(repository.findAll()).hasSize(1);
    }

    @Test
    void excluirAvaliacao_idInexistente_deveRetornar404() throws Exception {
        mockMvc.perform(delete("/avaliacoes/999999")
                        .with(jwt().jwt(j -> j.claim("https://social-insper.com/roles", List.of("ADMIN")))))
                .andExpect(status().isNotFound());
    }

    @Test
    void excluirAvaliacao_semAutenticacao_deveRetornar401() throws Exception {
        Avaliacao avaliacao = salvar("a@insper.edu.br", 7);

        mockMvc.perform(delete("/avaliacoes/" + avaliacao.getId()))
                .andExpect(status().isUnauthorized());

        assertThat(repository.findAll()).hasSize(1);
    }
}
