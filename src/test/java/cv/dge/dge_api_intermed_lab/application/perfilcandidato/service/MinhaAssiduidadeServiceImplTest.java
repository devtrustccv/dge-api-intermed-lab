package cv.dge.dge_api_intermed_lab.application.perfilcandidato.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cv.dge.dge_api_intermed_lab.application.document.service.ComboboxService;
import cv.dge.dge_api_intermed_lab.application.document.service.DocumentService;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaAssiduidadeDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaAssiduidadeFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaAssiduidadeListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaAssiduidadeOpcoesResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaAssiduidadeRequest;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.MinhaAssiduidadeRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.MinhaAssiduidadeRepository.AssiduidadeRegisto;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.MinhaAssiduidadeRepository.ColocacaoAtiva;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class MinhaAssiduidadeServiceImplTest {

    @Mock
    private MinhaAssiduidadeRepository assiduidadeRepository;

    @Mock
    private DocumentService documentService;

    @Mock
    private ComboboxService comboboxService;

    private MinhaAssiduidadeServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MinhaAssiduidadeServiceImpl(
                assiduidadeRepository,
                documentService,
                comboboxService
        );
        ReflectionTestUtils.setField(service, "appCodeDocumento", "interm_laboral");
        ReflectionTestUtils.setField(service, "estadoDocumento", "A");
        ReflectionTestUtils.setField(service, "tipoRelacaoDocumento", "EMPREGO_T_ASSIDUIDADE");
        ReflectionTestUtils.setField(service, "tipoComprovativoIdConfigurado", "");
    }

    @Test
    void deveListarRegistosDoCandidatoComFiltrosDescricoesEHorario() {
        MinhaAssiduidadeFiltro recebido = new MinhaAssiduidadeFiltro(
                9001L,
                "falta justificada",
                "pendente",
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31)
        );
        when(assiduidadeRepository.listar(argThat(filtro ->
                filtro.pessoaId().equals(9001L)
                        && "FALTA_JUSTIFICADA".equals(filtro.tipoAssiduidade())
                        && "PENDENTE".equals(filtro.estado())
        ))).thenReturn(List.of(registo(null)));

        List<MinhaAssiduidadeListaResponse> resultado = service.listar(recebido);

        assertThat(resultado).singleElement().satisfies(item -> {
            assertThat(item.assiduidadeId()).isEqualTo(41);
            assertThat(item.tipoAssiduidade()).isEqualTo("FALTA_JUSTIFICADA");
            assertThat(item.tipoAssiduidadeDescricao()).isEqualTo("Falta justificada");
            assertThat(item.horario()).isEqualTo("08:30 - 17:15");
            assertThat(item.estadoDescricao()).isEqualTo("Pendente");
        });
    }

    @Test
    void deveDisponibilizarSomenteOpcoesDosDominiosDaAssiduidade() {
        MinhaAssiduidadeOpcoesResponse resultado = service.listarOpcoes();

        assertThat(resultado.tiposAssiduidade())
                .extracting("valor")
                .containsExactly("FALTA", "PRESENTE", "FALTA_JUSTIFICADA");
        assertThat(resultado.estadosAssiduidade())
                .extracting("valor")
                .containsExactly("APROVADO", "INDEFERIDO", "PENDENTE");
    }

    @Test
    void deveCriarPendenteComColocacaoAtivaSemExigirComprovativo() {
        MinhaAssiduidadeRequest request = request();
        ColocacaoAtiva colocacao = new ColocacaoAtiva(12, 5, "Entidade X", 9001L, "Kevin Sousa");
        when(assiduidadeRepository.buscarColocacaoAtiva(9001L)).thenReturn(Optional.of(colocacao));
        when(assiduidadeRepository.inserir(eq(colocacao), argThat(dados ->
                "FALTA_JUSTIFICADA".equals(dados.tipoAssiduidade())
                        && "kevin@dge.cv".equals(dados.utilizador())
        ), eq("PENDENTE"))).thenReturn(41);
        when(assiduidadeRepository.buscarPorId(41, 9001L)).thenReturn(Optional.of(registo(null)));

        MinhaAssiduidadeDetalheResponse resultado = service.criar(9001L, request, null);

        assertThat(resultado.assiduidadeId()).isEqualTo(41);
        assertThat(resultado.estado()).isEqualTo("PENDENTE");
        assertThat(resultado.comprovativoPath()).isNull();
        verify(assiduidadeRepository, never()).atualizarComprovativo(eq(41), eq(9001L), argThat(path -> true));
        verifyNoInteractions(documentService, comboboxService);
    }

    @Test
    void deveGuardarComprovativoOpcionalPeloServicoDocumental() {
        MockMultipartFile comprovativo = new MockMultipartFile(
                "comprovativo",
                "Declaração médica.pdf",
                "application/pdf",
                "pdf".getBytes()
        );
        ColocacaoAtiva colocacao = new ColocacaoAtiva(12, 5, "Entidade X", 9001L, "Kevin Sousa");
        String path = "interm_laboral/2026/modulos/EMPREGO_T_ASSIDUIDADE/41/COMPROVATIVO-declaracao.pdf";
        when(assiduidadeRepository.buscarColocacaoAtiva(9001L)).thenReturn(Optional.of(colocacao));
        when(assiduidadeRepository.inserir(eq(colocacao), argThat(dados -> true), eq("PENDENTE")))
                .thenReturn(41);
        when(comboboxService.listarDocumentosAtivos()).thenReturn(List.of(Map.of(
                "id", 8,
                "tipo_documento_desc", "Comprovativo de assiduidade"
        )));
        when(documentService.save(argThat(documento ->
                documento.getIdRelacao().equals(41)
                        && "EMPREGO_T_ASSIDUIDADE".equals(documento.getTipoRelacao())
                        && "8".equals(documento.getIdTpDoc())
                        && documento.getFile() == comprovativo
        ))).thenReturn(path);
        when(assiduidadeRepository.atualizarComprovativo(41, 9001L, path)).thenReturn(true);
        when(assiduidadeRepository.buscarPorId(41, 9001L)).thenReturn(Optional.of(registo(path)));
        when(documentService.gerarLinkPublico(path)).thenReturn("https://documentos/41");

        MinhaAssiduidadeDetalheResponse resultado = service.criar(9001L, request(), comprovativo);

        assertThat(resultado.comprovativoPath()).isEqualTo(path);
        assertThat(resultado.comprovativoUrl()).isEqualTo("https://documentos/41");
        verify(assiduidadeRepository).atualizarComprovativo(41, 9001L, path);
    }

    @Test
    void deveImpedirRegistoSemColocacaoAtiva() {
        when(assiduidadeRepository.buscarColocacaoAtiva(9001L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(9001L, request(), null))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(ex.getReason()).contains("colocação ativa");
                });

        verify(assiduidadeRepository, never()).inserir(
                argThat(colocacao -> true),
                argThat(dados -> true),
                eq("PENDENTE")
        );
        verifyNoInteractions(documentService, comboboxService);
    }

    @Test
    void deveAtualizarSomenteCamposEditaveisEPreservarComprovativoQuandoNaoHaNovo() {
        when(assiduidadeRepository.buscarPorId(41, 9001L))
                .thenReturn(Optional.of(registo("documentos/anterior.pdf")))
                .thenReturn(Optional.of(registo("documentos/anterior.pdf")));
        when(assiduidadeRepository.atualizar(eq(41), eq(9001L), argThat(dados ->
                "FALTA_JUSTIFICADA".equals(dados.tipoAssiduidade())
        ))).thenReturn(true);
        when(documentService.gerarLinkPublico("documentos/anterior.pdf"))
                .thenReturn("https://documentos/anterior");

        MinhaAssiduidadeDetalheResponse resultado = service.atualizar(41, 9001L, request(), null);

        assertThat(resultado.comprovativoPath()).isEqualTo("documentos/anterior.pdf");
        assertThat(resultado.observacao()).isEqualTo("A aguardar validação.");
        verify(assiduidadeRepository, never()).atualizarComprovativo(eq(41), eq(9001L), argThat(path -> true));
        verifyNoInteractions(comboboxService);
    }

    @Test
    void deveOcultarRegistoQueNaoPertenceAoCandidato() {
        when(assiduidadeRepository.buscarPorId(41, 9001L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(41, 9001L))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(ex.getReason()).contains("não pertence ao candidato");
                });
    }

    @Test
    void deveRejeitarPeriodoInvalidoAntesDeConsultarBase() {
        MinhaAssiduidadeFiltro filtro = new MinhaAssiduidadeFiltro(
                9001L,
                null,
                null,
                LocalDate.of(2026, 8, 31),
                LocalDate.of(2026, 8, 1)
        );

        assertThatThrownBy(() -> service.listar(filtro))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex ->
                        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));

        verify(assiduidadeRepository, never()).listar(filtro);
    }

    private MinhaAssiduidadeRequest request() {
        return new MinhaAssiduidadeRequest(
                "falta justificada",
                LocalDate.of(2026, 8, 28),
                LocalTime.of(8, 30),
                LocalTime.of(17, 15),
                "Consulta médica.",
                "kevin@dge.cv"
        );
    }

    private AssiduidadeRegisto registo(String comprovativo) {
        return new AssiduidadeRegisto(
                41,
                12,
                5,
                "Entidade X",
                9001L,
                "Kevin Sousa",
                LocalDate.of(2026, 8, 28),
                LocalTime.of(8, 30),
                LocalTime.of(17, 15),
                "falta justificada",
                "Consulta médica.",
                "pendente",
                "A aguardar validação.",
                comprovativo,
                LocalDateTime.of(2026, 8, 28, 18, 0),
                "kevin@dge.cv",
                null,
                null
        );
    }
}
