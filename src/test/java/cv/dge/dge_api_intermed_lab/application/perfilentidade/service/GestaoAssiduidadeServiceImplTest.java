package cv.dge.dge_api_intermed_lab.application.perfilentidade.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.dge.dge_api_intermed_lab.application.document.service.DocumentService;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.assiduidade.AssiduidadeEstagiarioDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.assiduidade.AssiduidadeValidacaoRequest;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilentidade.repository.GestaoAssiduidadeRepository;
import cv.dge.dge_api_intermed_lab.support.EmpregoDominioTestFixture;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class GestaoAssiduidadeServiceImplTest {

    @Mock
    private GestaoAssiduidadeRepository repository;

    @Mock
    private DocumentService documentService;

    private GestaoAssiduidadeServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GestaoAssiduidadeServiceImpl(
                EmpregoDominioTestFixture.criar(),
                repository,
                documentService
        );
    }

    @Test
    void deveGravarEstadoRecusadoEObservacaoQuandoDecisaoForIndeferir() {
        when(repository.buscarPorId(1, 40))
                .thenReturn(Optional.of(detalhe("PENDENTE", null)))
                .thenReturn(Optional.of(detalhe("RECUSADO", "Comprovativo insuficiente.")));

        AssiduidadeEstagiarioDetalheResponse resultado = service.validar(
                1,
                40,
                new AssiduidadeValidacaoRequest(
                        "INDEFERIR",
                        "Comprovativo insuficiente.",
                        null,
                        "utilizador"
                )
        );

        verify(repository).validar(
                1,
                40,
                "RECUSADO",
                "Comprovativo insuficiente.",
                "utilizador"
        );
        assertThat(resultado.estado()).isEqualTo("RECUSADO");
        assertThat(resultado.observacao()).isEqualTo("Comprovativo insuficiente.");
    }

    @Test
    void deveGravarEstadoAprovadoQuandoDecisaoForAprovar() {
        when(repository.buscarPorId(1, 40))
                .thenReturn(Optional.of(detalhe("PENDENTE", null)))
                .thenReturn(Optional.of(detalhe("APROVADO", "Validado.")));

        service.validar(
                1,
                40,
                new AssiduidadeValidacaoRequest("APROVAR", null, "Validado.", "utilizador")
        );

        verify(repository).validar(1, 40, "APROVADO", "Validado.", "utilizador");
    }

    @Test
    void deveExigirMotivoQuandoDecisaoForIndeferir() {
        when(repository.buscarPorId(1, 40)).thenReturn(Optional.of(detalhe("PENDENTE", null)));

        assertThatThrownBy(() -> service.validar(
                1,
                40,
                new AssiduidadeValidacaoRequest("INDEFERIR", null, null, "utilizador")
        )).isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("motivo do indeferimento");

        verify(repository, never()).validar(1, 40, "RECUSADO", null, "utilizador");
    }

    private AssiduidadeEstagiarioDetalheResponse detalhe(String estado, String observacao) {
        return new AssiduidadeEstagiarioDetalheResponse(
                1,
                60,
                44,
                "Oferta",
                40,
                "Entidade",
                126L,
                "Estagiário",
                LocalDate.of(2026, 10, 20),
                LocalTime.of(8, 0),
                LocalTime.of(17, 0),
                "PRESENTE",
                null,
                null,
                estado,
                null,
                observacao,
                null,
                null,
                null,
                LocalDateTime.of(2026, 10, 20, 9, 0),
                "utilizador",
                null,
                null
        );
    }
}
