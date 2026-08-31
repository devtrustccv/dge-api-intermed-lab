package cv.dge.dge_api_intermed_lab.application.perfilcandidato.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaAvaliacaoDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaAvaliacaoListaResponse;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.MinhaAvaliacaoRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.MinhaAvaliacaoRepository.AvaliacaoDesempenhoRegisto;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.MinhaAvaliacaoRepository.AvaliacaoDetalheRegisto;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.MinhaAvaliacaoRepository.AvaliacaoListaRegisto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class MinhaAvaliacaoServiceImplTest {

    @Mock
    private MinhaAvaliacaoRepository avaliacaoRepository;

    private MinhaAvaliacaoServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MinhaAvaliacaoServiceImpl(avaliacaoRepository);
    }

    @Test
    void deveListarSomenteAvaliacoesDoCandidatoComDescricaoDoTipo() {
        LocalDateTime dataRegisto = LocalDateTime.of(2026, 8, 28, 14, 30);
        when(avaliacaoRepository.listar(9001L)).thenReturn(List.of(new AvaliacaoListaRegisto(
                41,
                "mensal",
                "Agosto de 2026",
                new BigDecimal("4.50"),
                dataRegisto
        )));

        List<MinhaAvaliacaoListaResponse> resultado = service.listar(9001L);

        verify(avaliacaoRepository).listar(9001L);
        assertThat(resultado).singleElement().satisfies(item -> {
            assertThat(item.avaliacaoId()).isEqualTo(41);
            assertThat(item.tipoAvaliacao()).isEqualTo("MENSAL");
            assertThat(item.tipoAvaliacaoDescricao()).isEqualTo("Mensal");
            assertThat(item.periodoReferencia()).isEqualTo("Agosto de 2026");
            assertThat(item.classificacao()).isEqualByComparingTo("4.50");
            assertThat(item.dataRegisto()).isEqualTo(dataRegisto);
        });
    }

    @Test
    void deveDevolverDetalheComDescricoesDosDominiosEDesempenho() {
        when(avaliacaoRepository.buscarPorId(41, 9001L)).thenReturn(Optional.of(new AvaliacaoDetalheRegisto(
                41,
                "TRIMESTRAL",
                "Junho a Agosto de 2026",
                List.of(
                        new AvaliacaoDesempenhoRegisto("comp tecnica", "4"),
                        new AvaliacaoDesempenhoRegisto("COMP_COMPORTAMENTAL", "5")
                ),
                "3",
                "Sim",
                new BigDecimal("4.75"),
                "Demonstrou boa evolução.",
                LocalDateTime.of(2026, 8, 28, 14, 30)
        )));

        MinhaAvaliacaoDetalheResponse resultado = service.buscarPorId(41, 9001L);

        assertThat(resultado.tipoAvaliacao()).isEqualTo("TRIMESTRAL");
        assertThat(resultado.tipoAvaliacaoDescricao()).isEqualTo("Trimestral");
        assertThat(resultado.grauSatisfacao()).isEqualTo("3");
        assertThat(resultado.grauSatisfacaoDescricao()).isEqualTo("Muito Satisfeito");
        assertThat(resultado.avaliacaoDesempenho()).hasSize(2);
        assertThat(resultado.avaliacaoDesempenho().get(0).tipoCompetencia()).isEqualTo("COMP_TECNICA");
        assertThat(resultado.avaliacaoDesempenho().get(0).tipoCompetenciaDescricao())
                .isEqualTo("Competência Técnica");
        assertThat(resultado.avaliacaoDesempenho().get(0).avaliacaoDescricao()).isEqualTo("Muito Bom");
        assertThat(resultado.avaliacaoDesempenho().get(1).avaliacaoDescricao()).isEqualTo("Excelente");
    }

    @Test
    void deveOcultarAvaliacaoQueNaoPertenceAoCandidato() {
        when(avaliacaoRepository.buscarPorId(41, 9001L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(41, 9001L))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(ex.getReason()).contains("não pertence ao candidato");
                });

        verify(avaliacaoRepository).buscarPorId(41, 9001L);
    }

    @Test
    void deveRejeitarPessoaOuAvaliacaoInvalidaAntesDeConsultarBase() {
        assertThatThrownBy(() -> service.listar(null))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex ->
                        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
        assertThatThrownBy(() -> service.buscarPorId(0, 9001L))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex ->
                        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));

        verify(avaliacaoRepository, never()).listar(null);
        verify(avaliacaoRepository, never()).buscarPorId(0, 9001L);
    }
}
