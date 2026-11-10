package br.insper.avaliacoes.dto;

public record AvaliacaoRequest(String emailAvaliado, String comentarios, Integer nota) {
}
