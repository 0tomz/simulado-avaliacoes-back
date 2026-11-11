package br.insper.avaliacoes.dto;

import br.insper.avaliacoes.entity.StatusAvaliacao;
import jakarta.validation.constraints.NotBlank;

public record ArtigoRequest(
        @NotBlank(message = "Título obrigatório") String titulo,
        @NotBlank(message = "Descrição obrigatória") String descricao,
        @NotBlank(message = "Prioridade obrigatória"), String prioridade
) {}
