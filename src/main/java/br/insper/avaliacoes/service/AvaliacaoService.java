package br.insper.avaliacoes.service;

import br.insper.avaliacoes.dto.AvaliacaoRequest;
import br.insper.avaliacoes.entity.Avaliacao;
import br.insper.avaliacoes.exception.ValidacaoAvaliacaoException;
import br.insper.avaliacoes.repository.AvaliacaoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AvaliacaoService {

    private final AvaliacaoRepository repository;

    public AvaliacaoService(AvaliacaoRepository repository) {
        this.repository = repository;
    }

    public Avaliacao criar(String emailAvaliador, AvaliacaoRequest req) {
        if (req.emailAvaliado() == null || req.emailAvaliado().isBlank()) {
            throw new ValidacaoAvaliacaoException("O email do avaliado é obrigatório");
        }
        if (req.comentarios() == null || req.comentarios().isBlank()) {
            throw new ValidacaoAvaliacaoException("Os comentários são obrigatórios");
        }
        if (req.nota() == null || req.nota() < 0 || req.nota() > 10) {
            throw new ValidacaoAvaliacaoException("A nota deve estar entre 0 e 10");
        }

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setEmailAvaliador(emailAvaliador);
        avaliacao.setEmailAvaliado(req.emailAvaliado());
        avaliacao.setComentarios(req.comentarios());
        avaliacao.setNota(req.nota());
        avaliacao.setDataAvaliacao(LocalDateTime.now());
        return repository.save(avaliacao);
    }

    public List<Avaliacao> listar() {
        return repository.findAllByOrderByDataAvaliacaoDesc();
    }
}
