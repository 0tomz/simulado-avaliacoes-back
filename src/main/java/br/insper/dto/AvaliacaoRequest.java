package br.insper.dto;

import jakarta.validation.constraints.NotBlank;

public record AvaliacaoRequest(
        @NotBlank(message = "Título obrigatório") String titulo,
        @NotBlank(message = "Descrição obrigatória") String descricao,
        @NotBlank(message = "Prioridade obrigatória") String prioridade
) {}
