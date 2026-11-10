package br.insper.avaliacoes.controller;

import br.insper.avaliacoes.dto.AvaliacaoRequest;
import br.insper.avaliacoes.entity.Avaliacao;
import br.insper.avaliacoes.service.AvaliacaoService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/avaliacoes")
@CrossOrigin(origins = "*")
public class AvaliacaoController {

    private final AvaliacaoService service;

    public AvaliacaoController(AvaliacaoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Avaliacao criarAvaliacao(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody AvaliacaoRequest request) {
        List<String> roles = jwt.getClaimAsStringList("https://social-insper.com/roles");

        if (roles == null || !roles.contains("ADMIN")) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Apenas ADMIN pode cadastrar avaliações"
            );
        }
        String email = jwt.getClaimAsString("https://social-insper.com/email");
        if (email == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email do avaliador não encontrado no token"
            );
        }
        return service.criar(email, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirAvaliacao(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {
        List<String> roles = jwt.getClaimAsStringList("https://social-insper.com/roles");

        if (roles == null || !roles.contains("ADMIN")) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Apenas ADMIN pode excluir avaliações"
            );
        }
        service.excluir(id);
    }

    @GetMapping
    public List<Avaliacao> listarAvaliacoes() {
        return service.listar();
    }
}
