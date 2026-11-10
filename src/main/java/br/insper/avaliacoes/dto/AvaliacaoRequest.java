package br.insper.avaliacoes.dto;

import br.insper.avaliacoes.entity.StatusAvaliacao;

public record AvaliacaoRequest(String emailAvaliado, String comentarios, Integer nota, StatusAvaliacao status) {
}
