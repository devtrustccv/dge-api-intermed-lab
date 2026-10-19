package cv.dge.dge_api_intermed_lab.application.perfilentidade.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.dge.dge_api_intermed_lab.application.document.dto.DocRelacaoDTO;
import cv.dge.dge_api_intermed_lab.application.document.service.DocumentService;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.colocacao.ColocacaoCandidatoFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.colocacao.ColocacaoCandidatoListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.colocacao.ColocacaoCandidatoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.colocacao.ColocacaoCandidatoResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.colocacao.ColocacaoCandidatoVinculo;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilentidade.repository.ColocacaoCandidatoRepository;
import cv.dge.dge_api_intermed_lab.support.EmpregoDominioTestFixture;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ColocacaoCandidatoServiceImplTest {

    private static final Integer ENTIDADE_ID = 40;

    @Mock
    private ColocacaoCandidatoRepository repository;
    @Mock
    private DocumentService documentService;

    private ColocacaoCandidatoServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ColocacaoCandidatoServiceImpl(
                EmpregoDominioTestFixture.criar(),
                repository,
                documentService
        );
        ReflectionTestUtils.setField(service, "appCodeDocumento", "interm_laboral");
        ReflectionTestUtils.setField(service, "estadoDocumento", "A");
        ReflectionTestUtils.setField(service, "tipoRelacaoDocumento", "EMPREGO_T_COLOCACAO_CANDIDATO");
    }

    @Test
    void deveGuardarReferenciaDoContratoEGerarLinkApenasNaResposta() {
        String path = "contratos/" + "a".repeat(160) + ".pdf";
        String link = "https://documentos/document-viewer?path_url=" + path + "&type=application/pdf";
        ColocacaoCandidatoRequest request = request(path);
        ColocacaoCandidatoVinculo vinculo = new ColocacaoCandidatoVinculo(
                15,
                126L,
                "Candidato",
                29,
                "OFERTA_ESTAGIO",
                "4/2026",
                ENTIDADE_ID,
                "Entidade"
        );

        when(repository.buscarVinculoCandidatura(29, 126L, ENTIDADE_ID))
                .thenReturn(Optional.of(vinculo));
        when(repository.inserir(any(), eq(vinculo), eq("A"), eq(false), eq("utilizador")))
                .thenReturn(60);
        when(repository.buscarPorId(60, ENTIDADE_ID))
                .thenReturn(Optional.of(detalhe(path)));
        when(documentService.gerarLinkPublico(path)).thenReturn(link);

        ColocacaoCandidatoResponse resposta = service.criar(ENTIDADE_ID, request);

        ArgumentCaptor<ColocacaoCandidatoRequest> dadosPersistidos =
                ArgumentCaptor.forClass(ColocacaoCandidatoRequest.class);
        verify(repository).inserir(
                dadosPersistidos.capture(),
                eq(vinculo),
                eq("A"),
                eq(false),
                eq("utilizador")
        );
        assertThat(dadosPersistidos.getValue().contratoPath()).isEqualTo(path);
        assertThat(resposta.contratoPath()).isEqualTo(link);
        verify(documentService).gerarLinkPublico(path);
    }

    @Test
    void deveGuardarContratoMultipartEDevolverLinkPublico() {
        String path = "interm_laboral/2026/modulos/EMPREGO_T_COLOCACAO_CANDIDATO/60/CONTRATO.pdf";
        String link = "https://documentos/document-viewer?path_url=" + path + "&type=application/pdf";
        ColocacaoCandidatoRequest request = request(null);
        ColocacaoCandidatoVinculo vinculo = new ColocacaoCandidatoVinculo(
                15, 126L, "Candidato", 29, "OFERTA_ESTAGIO", "4/2026", ENTIDADE_ID, "Entidade"
        );
        MockMultipartFile contrato = new MockMultipartFile(
                "contrato",
                "contrato.pdf",
                "application/pdf",
                "conteudo".getBytes()
        );

        when(repository.buscarVinculoCandidatura(29, 126L, ENTIDADE_ID))
                .thenReturn(Optional.of(vinculo));
        when(repository.inserir(any(), eq(vinculo), eq("A"), eq(false), eq("utilizador")))
                .thenReturn(60);
        when(documentService.save(any())).thenReturn(path);
        when(documentService.gerarLinkPublico(path)).thenReturn(link);
        when(documentService.gerarLinkPublico(link)).thenReturn(link);
        when(repository.buscarPorId(60, ENTIDADE_ID)).thenReturn(Optional.of(detalhe(link)));

        ColocacaoCandidatoResponse resposta = service.criar(ENTIDADE_ID, request, contrato);

        ArgumentCaptor<DocRelacaoDTO> documento = ArgumentCaptor.forClass(DocRelacaoDTO.class);
        verify(documentService).save(documento.capture());
        assertThat(documento.getValue().getIdRelacao()).isEqualTo(60);
        assertThat(documento.getValue().getTipoRelacao()).isEqualTo("EMPREGO_T_COLOCACAO_CANDIDATO");
        assertThat(documento.getValue().getFile()).isSameAs(contrato);
        verify(repository).atualizarContratoPath(60, ENTIDADE_ID, link, "utilizador");
        assertThat(resposta.contratoPath()).isEqualTo(link);
    }

    @Test
    void deveDevolverLinkPublicoDoContratoNaLista() {
        String path = "interm_laboral/2026/modulos/EMPREGO_T_COLOCACAO_CANDIDATO/60/CONTRATO.pdf";
        String link = "https://documentos/document-viewer?path_url=" + path + "&type=application/pdf";
        ColocacaoCandidatoFiltro filtro = new ColocacaoCandidatoFiltro(
                null, null, null, null, null, null, null, null, ENTIDADE_ID, null
        );
        when(repository.listar(any())).thenReturn(List.of(new ColocacaoCandidatoListaResponse(
                60,
                "OFERTA_EMPREGO",
                null,
                "19/2026",
                126L,
                "Candidato",
                "CONTRATO_TERMO",
                null,
                LocalDate.of(2026, 10, 1),
                path,
                LocalDateTime.of(2026, 10, 19, 16, 49)
        )));
        when(documentService.gerarLinkPublico(path)).thenReturn(link);

        List<ColocacaoCandidatoListaResponse> resposta = service.listar(filtro);

        assertThat(resposta).singleElement().satisfies(item ->
                assertThat(item.contratoPath()).isEqualTo(link)
        );
    }

    private ColocacaoCandidatoRequest request(String contratoPath) {
        return new ColocacaoCandidatoRequest(
                "OFERTA_ESTAGIO",
                29,
                "4/2026",
                126L,
                "CONTRATO_TERMO",
                6,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2027, 4, 1),
                "Descrição",
                contratoPath,
                "utilizador"
        );
    }

    private ColocacaoCandidatoResponse detalhe(String contratoPath) {
        return new ColocacaoCandidatoResponse(
                60,
                29,
                "OFERTA_ESTAGIO",
                null,
                "4/2026",
                ENTIDADE_ID,
                "Entidade",
                126L,
                "Candidato",
                15,
                "CONTRATO_TERMO",
                null,
                6,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2027, 4, 1),
                "Descrição",
                contratoPath,
                "A",
                null,
                false,
                LocalDateTime.of(2026, 10, 8, 18, 47),
                "utilizador",
                null,
                null
        );
    }
}
