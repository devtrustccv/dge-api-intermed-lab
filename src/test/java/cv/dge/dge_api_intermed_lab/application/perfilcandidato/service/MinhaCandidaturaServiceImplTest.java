package cv.dge.dge_api_intermed_lab.application.perfilcandidato.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cv.dge.dge_api_intermed_lab.application.document.service.DocumentService;
import cv.dge.dge_api_intermed_lab.application.geografia.service.GlobalGeografiaService;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaCandidaturaDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaCandidaturaFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaCandidaturaListaResponse;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.MinhaCandidaturaRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.MinhaCandidaturaRepository.CandidaturaRegisto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class MinhaCandidaturaServiceImplTest {

    @Mock
    private MinhaCandidaturaRepository candidaturaRepository;

    @Mock
    private GlobalGeografiaService globalGeografiaService;

    @Mock
    private DocumentService documentService;

    private MinhaCandidaturaServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MinhaCandidaturaServiceImpl(
                candidaturaRepository,
                globalGeografiaService,
                documentService
        );
    }

    @Test
    void deveListarApenasDadosDoCandidatoComDominiosEGeografiasEnriquecidos() {
        LocalDateTime dataCandidatura = LocalDateTime.of(2026, 8, 20, 10, 30);
        when(candidaturaRepository.listar(any())).thenReturn(List.of(registo(
                15,
                "EMPREGO",
                "APROVADA",
                "PORTAL",
                "101",
                "102",
                null,
                dataCandidatura
        )));
        when(globalGeografiaService.buscarNomePorCodigo("101")).thenReturn(Optional.of("Santiago"));
        when(globalGeografiaService.buscarNomePorCodigo("102")).thenReturn(Optional.of("Praia"));

        List<MinhaCandidaturaListaResponse> resultado = service.listar(new MinhaCandidaturaFiltro(
                9001L,
                "emprego",
                45,
                "101",
                "102",
                "aprovada",
                " REF-2026 ",
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31)
        ));

        ArgumentCaptor<MinhaCandidaturaFiltro> filtroCaptor = ArgumentCaptor.forClass(MinhaCandidaturaFiltro.class);
        verify(candidaturaRepository).listar(filtroCaptor.capture());
        assertThat(filtroCaptor.getValue().pessoaId()).isEqualTo(9001L);
        assertThat(filtroCaptor.getValue().tipoOferta()).isEqualTo("OFERTA_EMPREGO");
        assertThat(filtroCaptor.getValue().estado()).isEqualTo("APROVADO");
        assertThat(filtroCaptor.getValue().codigoReferencia()).isEqualTo("REF-2026");

        assertThat(resultado).singleElement().satisfies(item -> {
            assertThat(item.candidaturaId()).isEqualTo(15);
            assertThat(item.tipoOferta()).isEqualTo("OFERTA_EMPREGO");
            assertThat(item.tipoOfertaDescricao()).isEqualTo("Oferta Emprego");
            assertThat(item.ilhaId()).isEqualTo("101");
            assertThat(item.ilha()).isEqualTo("Santiago");
            assertThat(item.concelhoId()).isEqualTo("102");
            assertThat(item.concelho()).isEqualTo("Praia");
            assertThat(item.estado()).isEqualTo("APROVADO");
            assertThat(item.estadoDescricao()).isEqualTo("Aprovado");
            assertThat(item.dataCandidatura()).isEqualTo(dataCandidatura);
        });
    }

    @Test
    void deveBloquearDetalheQueNaoPertenceAoCandidato() {
        when(candidaturaRepository.buscarPorId(77, 9001L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(77, 9001L))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(ex.getReason()).contains("não pertence ao candidato");
                });

        verify(candidaturaRepository).buscarPorId(77, 9001L);
    }

    @Test
    void deveDevolverDetalheComMotivoCanalEAnexosNormalizados() throws Exception {
        JsonNode anexos = new ObjectMapper().readTree("""
                {
                  "curriculumVitae": {
                    "tipo": "CURRICULO_VITAE",
                    "nome": "cv.pdf",
                    "path": "/candidaturas/77/cv.pdf"
                  },
                  "outrosDocumentos": [
                    {
                      "nome": "diploma.pdf",
                      "path": "/candidaturas/77/diploma.pdf",
                      "url": "https://documentos.test/diploma"
                    }
                  ]
                }
                """);
        when(candidaturaRepository.buscarPorId(77, 9001L)).thenReturn(Optional.of(registo(
                77,
                "OFERTA_ESTAGIO",
                "RECUSADO",
                "ONLINE",
                "101",
                "102",
                anexos,
                LocalDateTime.of(2026, 8, 21, 9, 0)
        )));
        when(globalGeografiaService.buscarNomePorCodigo("101")).thenReturn(Optional.of("Santiago"));
        when(globalGeografiaService.buscarNomePorCodigo("102")).thenReturn(Optional.of("Praia"));
        when(documentService.gerarLinkPublico("/candidaturas/77/cv.pdf"))
                .thenReturn("https://documentos.test/cv");

        MinhaCandidaturaDetalheResponse resultado = service.buscarPorId(77, 9001L);

        assertThat(resultado.estado()).isEqualTo("RECUSADO");
        assertThat(resultado.motivoRecusa()).isEqualTo("Perfil não corresponde aos requisitos.");
        assertThat(resultado.canal()).isEqualTo("PORTAL");
        assertThat(resultado.canalDescricao()).isEqualTo("Portal");
        assertThat(resultado.anexos()).hasSize(2);
        assertThat(resultado.anexos().get(0).tipo()).isEqualTo("CURRICULO_VITAE");
        assertThat(resultado.anexos().get(0).url()).isEqualTo("https://documentos.test/cv");
        assertThat(resultado.anexos().get(1).tipo()).isEqualTo("OUTRO_DOCUMENTO");
        assertThat(resultado.anexos().get(1).url()).isEqualTo("https://documentos.test/diploma");
    }

    @Test
    void deveRejeitarIntervaloDeDatasInvertidoAntesDeConsultarBase() {
        MinhaCandidaturaFiltro filtro = new MinhaCandidaturaFiltro(
                9001L,
                null,
                null,
                null,
                null,
                null,
                null,
                LocalDate.of(2026, 8, 31),
                LocalDate.of(2026, 8, 1)
        );

        assertThatThrownBy(() -> service.listar(filtro))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex ->
                        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));

        verify(candidaturaRepository, never()).listar(any());
    }

    @Test
    void deveExigirIdentificacaoValidaDoCandidato() {
        MinhaCandidaturaFiltro filtro = new MinhaCandidaturaFiltro(
                null, null, null, null, null, null, null, null, null
        );

        assertThatThrownBy(() -> service.listar(filtro))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex ->
                        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));

        verify(candidaturaRepository, never()).listar(any());
    }

    private CandidaturaRegisto registo(
            Integer id,
            String tipoOferta,
            String estado,
            String canal,
            String ilha,
            String concelho,
            JsonNode anexos,
            LocalDateTime dataCandidatura
    ) {
        return new CandidaturaRegisto(
                id,
                tipoOferta,
                501,
                "Programador Java",
                "REF-2026-001",
                45,
                "Entidade Exemplo",
                ilha,
                concelho,
                estado,
                "Perfil não corresponde aos requisitos.",
                canal,
                anexos,
                dataCandidatura
        );
    }
}
