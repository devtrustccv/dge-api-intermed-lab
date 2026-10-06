package cv.dge.dge_api_intermed_lab.application.perfilentidade.service;

import cv.dge.dge_api_intermed_lab.support.EmpregoDominioTestFixture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cv.dge.dge_api_intermed_lab.application.document.service.DocumentService;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.CandidaturaAvaliacaoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.CandidaturaDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.CandidaturaFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.CandidaturaListaResponse;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilentidade.repository.GestaoCandidaturaRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class GestaoCandidaturaServiceImplTest {

    @Mock
    private GestaoCandidaturaRepository candidaturaRepository;

    @Mock
    private DocumentService documentService;

    private GestaoCandidaturaServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GestaoCandidaturaServiceImpl(EmpregoDominioTestFixture.criar(), candidaturaRepository, documentService);
    }

    @Test
    void deveEntregarTodosOsAnexosGuardadosNaCandidaturaComUrlPublico() {
        String pathCv = "interm_laboral/2026/processos/candidatura/1/cv.pdf";
        String pathCarta = "interm_laboral/2026/processos/candidatura/1/carta.pdf";
        Map<String, Object> anexosGuardados = Map.of(
                "curriculumVitae", Map.of(
                        "tipo", "CURRICULO_VITAE",
                        "nome", "cv.pdf",
                        "path", pathCv
                ),
                "outrosDocumentos", List.of(Map.of(
                        "tipo", "OUTRO_DOCUMENTO",
                        "nome", "carta.pdf",
                        "path", pathCarta
                ))
        );
        when(candidaturaRepository.listar(filtroVazio())).thenReturn(List.of(candidatura(anexosGuardados)));
        when(documentService.gerarLinkPublico(pathCv)).thenReturn("https://sgf/view?path=cv");
        when(documentService.gerarLinkPublico(pathCarta)).thenReturn("https://sgf/view?path=carta");

        CandidaturaListaResponse resultado = service.listar(filtroVazio()).get(0);

        assertThat(resultado.sexoCandidato()).isEqualTo("Feminino");
        assertThat(resultado.tipoDocumento()).isEqualTo("CURRICULO_VITAE");
        assertThat(resultado.anexo()).isEqualTo(resultado.anexos().get(0));
        assertThat(resultado.anexos())
                .extracting("tipo", "nome", "path", "url")
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(
                                "CURRICULO_VITAE", "cv.pdf", pathCv, "https://sgf/view?path=cv"),
                        org.assertj.core.groups.Tuple.tuple(
                                "OUTRO_DOCUMENTO", "carta.pdf", pathCarta, "https://sgf/view?path=carta")
                );
    }

    @Test
    void deveManterAnexosVaziosQuandoNaoForamGuardadosNaCandidatura() {
        when(candidaturaRepository.listar(filtroVazio())).thenReturn(List.of(candidatura(null)));

        CandidaturaListaResponse resultado = service.listar(filtroVazio()).get(0);

        assertThat(resultado.tipoDocumento()).isNull();
        assertThat(resultado.anexos()).isEmpty();
        assertThat(resultado.anexo()).isNull();
        verifyNoInteractions(documentService);
    }

    @Test
    void deveIndicarQuandoCadaTipoDeOfertaPodeSerAvaliado() {
        when(candidaturaRepository.listar(filtroVazio())).thenReturn(List.of(
                candidatura("emprego.pdf", "OFERTA_EMPREGO", false),
                candidatura("estagio-selecionado.pdf", "OFERTA_ESTAGIO", true),
                candidatura("estagio-nao-selecionado.pdf", "OFERTA_ESTAGIO", false)
        ));

        List<CandidaturaListaResponse> resultado = service.listar(filtroVazio());

        assertThat(resultado)
                .extracting(CandidaturaListaResponse::podeAvaliar)
                .containsExactly(true, true, false);
    }

    @Test
    void devePermitirAvaliarOfertaDeEmpregoSemSelecaoIefp() {
        when(candidaturaRepository.buscarPorId(1, 23))
                .thenReturn(Optional.of(candidaturaDetalhe("OFERTA_EMPREGO", false)));

        service.avaliar(1, 23, new CandidaturaAvaliacaoRequest("APROVADO", null, "utilizador"));

        verify(candidaturaRepository).atualizarAvaliacao(
                1, 23, "APROVADO", null, "utilizador"
        );
    }

    @Test
    void deveImpedirAvaliacaoDeEstagioNaoSelecionadoPeloIefp() {
        when(candidaturaRepository.buscarPorId(1, 23))
                .thenReturn(Optional.of(candidaturaDetalhe("OFERTA_ESTAGIO", false)));

        assertThatThrownBy(() -> service.avaliar(
                1,
                23,
                new CandidaturaAvaliacaoRequest("APROVADO", null, "utilizador")
        )).isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("selecionada pelo IEFP");

        verify(candidaturaRepository, never()).atualizarAvaliacao(
                1, 23, "APROVADO", null, "utilizador"
        );
    }

    @Test
    void deveRecusarDataFimAnteriorADataInicio() {
        CandidaturaFiltro filtro = new CandidaturaFiltro(
                23, null, null, null, null, null, null, null,
                LocalDate.of(2026, 9, 30), LocalDate.of(2026, 9, 1));

        assertThatThrownBy(() -> service.listar(filtro))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("data final");
        verifyNoInteractions(candidaturaRepository);
    }

    private CandidaturaFiltro filtroVazio() {
        return new CandidaturaFiltro(23, null, null, null, null, null, null, null, null, null);
    }

    private CandidaturaListaResponse candidatura(Object anexos) {
        return candidatura(anexos, "OFERTA_EMPREGO", null);
    }

    private CandidaturaListaResponse candidatura(Object anexos, String tipoOferta, Boolean selecaoIefp) {
        return new CandidaturaListaResponse(
                1,
                143L,
                "Candidato",
                LocalDate.of(1995, 4, 12),
                "F",
                "candidato@example.cv",
                "9912345",
                "Santiago / Praia",
                "Praia",
                "LICENCIATURA",
                tipoOferta,
                tipoOferta,
                22,
                "OF-2026-001",
                "Programador",
                "PORTAL",
                "PORTAL",
                null,
                anexos,
                null,
                "TRIAGEM",
                "TRIAGEM",
                null,
                selecaoIefp,
                null,
                null,
                null,
                false,
                LocalDateTime.of(2026, 8, 31, 15, 14, 50)
        );
    }

    private CandidaturaDetalheResponse candidaturaDetalhe(String tipoOferta, Boolean selecaoIefp) {
        return new CandidaturaDetalheResponse(
                1,
                tipoOferta,
                tipoOferta,
                22,
                "OF-2026-001",
                "Programador",
                23,
                "Entidade",
                LocalDateTime.of(2026, 8, 31, 15, 14, 50),
                null,
                null,
                "TRIAGEM",
                "TRIAGEM",
                null,
                selecaoIefp,
                null,
                null,
                LocalDateTime.of(2026, 8, 31, 15, 14, 50),
                "utilizador",
                null,
                null
        );
    }
}
