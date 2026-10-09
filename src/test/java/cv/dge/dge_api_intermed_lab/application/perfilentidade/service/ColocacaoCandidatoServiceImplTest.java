package cv.dge.dge_api_intermed_lab.application.perfilentidade.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.dge.dge_api_intermed_lab.application.document.service.DocumentService;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.ColocacaoCandidatoRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.ColocacaoCandidatoResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.ColocacaoCandidatoVinculo;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilentidade.repository.ColocacaoCandidatoRepository;
import cv.dge.dge_api_intermed_lab.support.EmpregoDominioTestFixture;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
