package br.insper.avaliacoes.repository;

import br.insper.avaliacoes.entity.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    List<Avaliacao> findAllByOrderByDataAvaliacaoDesc();

}
