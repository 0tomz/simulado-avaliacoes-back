package br.insper.avaliacoes.service;

import br.insper.avaliacoes.dto.AvaliacaoRequest;
import br.insper.avaliacoes.entity.Avaliacao;
import br.insper.avaliacoes.exception.ValidacaoAvaliacaoException;
import br.insper.avaliacoes.repository.AvaliacaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AvaliacaoServiceTests {

    @InjectMocks
    private AvaliacaoService avaliacaoService;

    @Mock
    private AvaliacaoRepository avaliacaoRepository;

    @Test
    public void testCriarComSucesso() {
        AvaliacaoRequest req = new AvaliacaoRequest("avaliado@insper.edu.br", "Muito bom", 8);

        when(avaliacaoRepository.save(any(Avaliacao.class))).thenAnswer(i -> i.getArgument(0));

        Avaliacao resultado = avaliacaoService.criar("avaliador@insper.edu.br", req);

        assertEquals("avaliador@insper.edu.br", resultado.getEmailAvaliador());
        assertEquals("avaliado@insper.edu.br", resultado.getEmailAvaliado());
        assertEquals("Muito bom", resultado.getComentarios());
        assertEquals(8, resultado.getNota());
        assertNotNull(resultado.getDataAvaliacao());
        assertFalse(resultado.getDataAvaliacao().isAfter(LocalDateTime.now()));
        verify(avaliacaoRepository).save(any(Avaliacao.class));
    }

    @Test
    public void testCriarComNotasNosLimites() {
        when(avaliacaoRepository.save(any(Avaliacao.class))).thenAnswer(i -> i.getArgument(0));

        assertEquals(0, avaliacaoService.criar("a@insper.edu.br", new AvaliacaoRequest("b@insper.edu.br", "ok", 0)).getNota());
        assertEquals(10, avaliacaoService.criar("a@insper.edu.br", new AvaliacaoRequest("b@insper.edu.br", "ok", 10)).getNota());
    }

    @Test
    public void testCriarComNotaNegativaLancaExcecao() {
        AvaliacaoRequest req = new AvaliacaoRequest("avaliado@insper.edu.br", "Muito bom", -1);

        assertThrows(ValidacaoAvaliacaoException.class, () -> avaliacaoService.criar("avaliador@insper.edu.br", req));
        verify(avaliacaoRepository, never()).save(any(Avaliacao.class));
    }

    @Test
    public void testCriarComNotaMaiorQueDezLancaExcecao() {
        AvaliacaoRequest req = new AvaliacaoRequest("avaliado@insper.edu.br", "Muito bom", 11);

        assertThrows(ValidacaoAvaliacaoException.class, () -> avaliacaoService.criar("avaliador@insper.edu.br", req));
        verify(avaliacaoRepository, never()).save(any(Avaliacao.class));
    }

    @Test
    public void testCriarComNotaNulaLancaExcecao() {
        AvaliacaoRequest req = new AvaliacaoRequest("avaliado@insper.edu.br", "Muito bom", null);

        assertThrows(ValidacaoAvaliacaoException.class, () -> avaliacaoService.criar("avaliador@insper.edu.br", req));
        verify(avaliacaoRepository, never()).save(any(Avaliacao.class));
    }

    @Test
    public void testCriarComEmailAvaliadoVazioLancaExcecao() {
        AvaliacaoRequest req = new AvaliacaoRequest("", "Muito bom", 5);

        assertThrows(ValidacaoAvaliacaoException.class, () -> avaliacaoService.criar("avaliador@insper.edu.br", req));
        verify(avaliacaoRepository, never()).save(any(Avaliacao.class));
    }

    @Test
    public void testCriarComEmailAvaliadoNuloLancaExcecao() {
        AvaliacaoRequest req = new AvaliacaoRequest(null, "Muito bom", 5);

        assertThrows(ValidacaoAvaliacaoException.class, () -> avaliacaoService.criar("avaliador@insper.edu.br", req));
        verify(avaliacaoRepository, never()).save(any(Avaliacao.class));
    }

    @Test
    public void testCriarComComentariosVaziosLancaExcecao() {
        AvaliacaoRequest req = new AvaliacaoRequest("avaliado@insper.edu.br", "", 5);

        assertThrows(ValidacaoAvaliacaoException.class, () -> avaliacaoService.criar("avaliador@insper.edu.br", req));
        verify(avaliacaoRepository, never()).save(any(Avaliacao.class));
    }

    @Test
    public void testCriarComComentariosNulosLancaExcecao() {
        AvaliacaoRequest req = new AvaliacaoRequest("avaliado@insper.edu.br", null, 5);

        assertThrows(ValidacaoAvaliacaoException.class, () -> avaliacaoService.criar("avaliador@insper.edu.br", req));
        verify(avaliacaoRepository, never()).save(any(Avaliacao.class));
    }

    @Test
    public void testListar() {
        Avaliacao avaliacao = new Avaliacao();
        when(avaliacaoRepository.findAllByOrderByDataAvaliacaoDesc()).thenReturn(List.of(avaliacao));

        List<Avaliacao> resultado = avaliacaoService.listar();

        assertEquals(1, resultado.size());
        verify(avaliacaoRepository).findAllByOrderByDataAvaliacaoDesc();
    }
}
