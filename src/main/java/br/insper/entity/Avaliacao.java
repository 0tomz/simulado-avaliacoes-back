package br.insper.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "artigos")
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String titulo;
    private String descricao;
    private String prioridade;
    private String emailCriador;
    private LocalDateTime dataCriacao;

    public Avaliacao(){}

    public Avaliacao(String titulo, String descricao, String prioridade, String emailCriador, LocalDateTime dataCriacao) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.prioridade = prioridade;
        this.emailCriador = emailCriador;
        this.dataCriacao = dataCriacao;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getPrioridade() { return prioridade; }
    public LocalDateTime setPrioridade(String prioridade) { this.prioridade = prioridade;}

    public String getEmailCriador() { return emailCriador; }
    public void setEmailCriador(String emailCriador) { this.emailCriador = emailCriador; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

}
