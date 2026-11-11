package br.insper.controller;

import br.insper.dto.AvaliacaoRequest;
import br.insper.entity.Avaliacao;
import br.insper.service.AvaliacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/artigos")
@CrossOrigin(origins = "*")
public class AvaliacaoController {

    private final AvaliacaoService service;

    public AvaliacaoController(AvaliacaoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Avaliacao criar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AvaliacaoRequest request) {
        validarAdmin(jwt);
        String email = jwt.getClaimAsString("https://social-insper.com");
        return service.criar(request, email);
    }

    @GetMapping
    public List<Avaliacao> listar() {
        return service.listarTodos();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        validarAdmin(jwt);
        service.deletar(id);
    }


    private void validarAdmin(Jwt jwt) {
        // Busca a lista de claims diretamente do corpo bruto do Token JWT do Auth0
        List<String> roles = jwt.getClaimAsStringList("https://social-insper.com");

        // Se não encontrar com o namespace, tenta ler do claim padrão do Auth0 por segurança
        if (roles == null) {
            roles = jwt.getClaimAsStringList("roles");
        }

        // Se mesmo assim for nulo ou não contiver a palavra ADMIN, barra com 403
        if (roles == null || !roles.contains("ADMIN")) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Apenas ADMIN pode executar esta ação"
            );
        }
    }
}
