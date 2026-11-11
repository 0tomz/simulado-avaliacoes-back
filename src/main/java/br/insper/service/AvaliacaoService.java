package br.insper.service;

import br.insper.dto.AvaliacaoRequest;
import br.insper.entity.Avaliacao;
import br.insper.repository.AvaliacaoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AvaliacaoService {

    private final AvaliacaoRepository repository;

    public AvaliacaoService(AvaliacaoRepository repository) {
        this.repository = repository;
    }

    public Avaliacao criar(AvaliacaoRequest request, String emailCriador) {
        Avaliacao avaliacao = new Avaliacao(
                request.titulo(),
                request.descricao(),
                request.prioridade(),
                emailCriador,
                LocalDateTime.now()
        );
        return repository.save(avaliacao);
    }

    public List<Avaliacao> listarTodos() {
        return repository.findAll();
    }

    public void deletar(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Artigo não encontrado");
        }
        repository.deleteById(id);
    }


}
