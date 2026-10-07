package cv.dge.dge_api_intermed_lab.application.perfilentidade.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.dge.dge_api_intermed_lab.application.document.dto.DocRelacaoDTO;
import cv.dge.dge_api_intermed_lab.application.document.service.DocumentService;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.RelatorioAcompanhamentoVinculo;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilentidade.repository.GestaoRelatorioAcompanhamentoRepository;
import cv.dge.dge_api_intermed_lab.support.EmpregoDominioTestFixture;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class GestaoRelatorioAcompanhamentoServiceImplTest {

    @Mock
    private GestaoRelatorioAcompanhamentoRepository repository;
    @Mock
    private DocumentService documentService;

    private GestaoRelatorioAcompanhamentoServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GestaoRelatorioAcompanhamentoServiceImpl(
                EmpregoDominioTestFixture.criar(),
                repository,
                documentService
        );
        ReflectionTestUtils.setField(service, "appCodeDocumento", "interm_laboral");
        ReflectionTestUtils.setField(service, "tipoRelacaoDocumento", "EMPREGO_T_RELATORIO_ACOMP");
        ReflectionTestUtils.setField(service, "estadoDocumento", "A");
    }

    @Test
    void deveGuardarSomentePathNoDocRelacaoELinkCompletoNoNegocio() {
        MockMultipartFile ficheiro = new MockMultipartFile(
                "relatorioAnexo",
                "relatorio.pdf",
                "application/pdf",
                "pdf".getBytes()
        );
        RelatorioAcompanhamentoVinculo vinculo = new RelatorioAcompanhamentoVinculo(
                20, "4/2026", 30, 40, "Entidade", 50L, "Estagiário"
        );
        String path = "interm_laboral/2026/modulos/EMPREGO_T_RELATORIO_ACOMP/60/RELATORIO.pdf";
        String link = "https://dge/document-viewer?path_url=" + path + "&type=application/pdf";

        when(repository.buscarVinculo(40, 50L, "4/2026")).thenReturn(Optional.of(vinculo));
        when(repository.inserir(eq(vinculo), any(), eq("A"), eq("utilizador"))).thenReturn(60);
        when(documentService.save(any())).thenReturn(path);
        when(documentService.gerarLinkPublico(path)).thenReturn(link);
        when(documentService.gerarLinkPublico(link)).thenReturn(link);
        when(repository.buscarPorId(60, 40)).thenReturn(Optional.of(detalhe(link)));

        RelatorioAcompanhamentoDetalheResponse resposta = service.criar(40, request(null), ficheiro);

        ArgumentCaptor<DocRelacaoDTO> documento = ArgumentCaptor.forClass(DocRelacaoDTO.class);
        verify(documentService).save(documento.capture());
        assertThat(documento.getValue().getPath())
                .startsWith("interm_laboral/2026/modulos/EMPREGO_T_RELATORIO_ACOMP/60/")
                .doesNotStartWith("http")
                .doesNotStartWith("data:");
        verify(repository).atualizarRelatorioAnexo(60, 40, link, "utilizador");
        assertThat(resposta.relatorioAnexo()).isEqualTo(link);
    }

    @Test
    void deveRejeitarPdfEmBase64SemPersistir() {
        RelatorioAcompanhamentoRequest request = request("data:application/pdf;base64,JVBERi0xLjYK");

        assertThatThrownBy(() -> service.criar(40, request))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(ex.getReason()).contains("multipart/form-data");
                });

        verify(repository, never()).inserir(any(), any(), any(), any());
        verify(documentService, never()).save(any());
    }

    private RelatorioAcompanhamentoRequest request(String relatorioAnexo) {
        return new RelatorioAcompanhamentoRequest(
                50L,
                "4/2026",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30),
                "Atividades",
                "Dificuldades",
                "Recomendações",
                relatorioAnexo,
                "utilizador"
        );
    }

    private RelatorioAcompanhamentoDetalheResponse detalhe(String relatorioAnexo) {
        return new RelatorioAcompanhamentoDetalheResponse(
                60,
                20,
                "4/2026",
                30,
                40,
                "Entidade",
                50L,
                "Estagiário",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30),
                "Atividades",
                "Dificuldades",
                "Recomendações",
                relatorioAnexo,
                "A",
                "A",
                null,
                "utilizador",
                null,
                null
        );
    }
}
