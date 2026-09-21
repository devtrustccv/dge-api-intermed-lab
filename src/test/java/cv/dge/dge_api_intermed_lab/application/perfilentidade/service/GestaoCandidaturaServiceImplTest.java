package cv.dge.dge_api_intermed_lab.application.perfilentidade.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cv.dge.dge_api_intermed_lab.application.document.dto.DocumentoResponseDTO;
import cv.dge.dge_api_intermed_lab.application.document.service.DocumentService;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.CandidaturaFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.CandidaturaListaResponse;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilentidade.repository.GestaoCandidaturaRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class GestaoCandidaturaServiceImplTest {

    private static final String TIPO_RELACAO = "EMPREGO_T_CANDIDATURA_OFERTA";
    private static final String APP_CODE = "interm_laboral";

    @Mock
    private GestaoCandidaturaRepository candidaturaRepository;

    @Mock
    private DocumentService documentService;

    private GestaoCandidaturaServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GestaoCandidaturaServiceImpl(candidaturaRepository, documentService);
        ReflectionTestUtils.setField(service, "tipoRelacaoDocumento", TIPO_RELACAO);
        ReflectionTestUtils.setField(service, "appCodeDocumento", APP_CODE);
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
        verify(documentService, never()).getDocumentosPorRelacao(1, TIPO_RELACAO, APP_CODE);
    }

    @Test
    void deveConsultarRelacaoDocumentalQuandoJsonDeAnexosEstaVazio() {
        DocumentoResponseDTO documento = DocumentoResponseDTO.builder()
                .id(91L)
                .idTpDoc("12")
                .name("curriculo.pdf")
                .path("interm_laboral/2026/processos/candidatura/1/curriculo.pdf")
                .previewUrl("https://sgf/view?path=curriculo")
                .build();
        when(candidaturaRepository.listar(filtroVazio())).thenReturn(List.of(candidatura(null)));
        when(documentService.getDocumentosPorRelacao(1, TIPO_RELACAO, APP_CODE))
                .thenReturn(List.of(documento));

        CandidaturaListaResponse resultado = service.listar(filtroVazio()).get(0);

        assertThat(resultado.tipoDocumento()).isEqualTo("12");
        assertThat(resultado.anexos()).singleElement().satisfies(anexo -> {
            assertThat(anexo.nome()).isEqualTo("curriculo.pdf");
            assertThat(anexo.path()).isEqualTo(documento.getPath());
            assertThat(anexo.url()).isEqualTo(documento.getPreviewUrl());
        });
        assertThat(resultado.anexo()).isEqualTo(resultado.anexos().get(0));
        verify(documentService).getDocumentosPorRelacao(1, TIPO_RELACAO, APP_CODE);
    }

    @Test
    void deveRecusarDataFimAnteriorADataInicio() {
        CandidaturaFiltro filtro = new CandidaturaFiltro(
                23, null, null, null, null, null, null,
                LocalDate.of(2026, 9, 30), LocalDate.of(2026, 9, 1));

        assertThatThrownBy(() -> service.listar(filtro))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("data final");
        verifyNoInteractions(candidaturaRepository);
    }

    private CandidaturaFiltro filtroVazio() {
        return new CandidaturaFiltro(23, null, null, null, null, null, null, null, null);
    }

    private CandidaturaListaResponse candidatura(Object anexos) {
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
                "OFERTA_EMPREGO",
                "OFERTA_EMPREGO",
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
                null,
                null,
                null,
                null,
                false,
                LocalDateTime.of(2026, 8, 31, 15, 14, 50)
        );
    }
}
