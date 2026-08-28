package cv.dge.dge_api_intermed_lab.application.perfilcandidato.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.dge.dge_api_intermed_lab.application.document.service.DocumentService;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaColocacaoDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaColocacaoListaResponse;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.MinhaColocacaoRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.MinhaColocacaoRepository.ColocacaoRegisto;
import java.time.LocalDate;
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
class MinhaColocacaoServiceImplTest {

    @Mock
    private MinhaColocacaoRepository colocacaoRepository;

    @Mock
    private DocumentService documentService;

    private MinhaColocacaoServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MinhaColocacaoServiceImpl(colocacaoRepository, documentService);
    }

    @Test
    void deveListarColocacoesDoCandidatoComTipoEContratoVisualizavel() {
        when(colocacaoRepository.listar(9001L)).thenReturn(List.of(registo(
                31,
                "ESTAGIO",
                "CONTRATO_TERMO",
                "A",
                "/contratos/31/contrato.pdf"
        )));
        when(documentService.gerarLinkPublico("/contratos/31/contrato.pdf"))
                .thenReturn("https://documentos.test/contrato-31");

        List<MinhaColocacaoListaResponse> resultado = service.listar(9001L);

        verify(colocacaoRepository).listar(9001L);
        assertThat(resultado).singleElement().satisfies(item -> {
            assertThat(item.colocacaoId()).isEqualTo(31);
            assertThat(item.ofertaId()).isEqualTo(501);
            assertThat(item.tipoOferta()).isEqualTo("OFERTA_ESTAGIO");
            assertThat(item.tipoOfertaDescricao()).isEqualTo("Oferta estágio");
            assertThat(item.titulo()).isEqualTo("Estágio em desenvolvimento");
            assertThat(item.codigoReferencia()).isEqualTo("REF-EST-2026-01");
            assertThat(item.contratoPath()).isEqualTo("/contratos/31/contrato.pdf");
            assertThat(item.contratoUrl()).isEqualTo("https://documentos.test/contrato-31");
        });
    }

    @Test
    void deveDevolverDetalheComDescricoesDosDominios() {
        when(colocacaoRepository.buscarPorId(31, 9001L)).thenReturn(Optional.of(registo(
                31,
                "OFERTA_ESTAGIO",
                "TERMO",
                "ATIVO",
                null
        )));

        MinhaColocacaoDetalheResponse resultado = service.buscarPorId(31, 9001L);

        assertThat(resultado.tipoOferta()).isEqualTo("OFERTA_ESTAGIO");
        assertThat(resultado.tipoOfertaDescricao()).isEqualTo("Oferta estágio");
        assertThat(resultado.tipoContrato()).isEqualTo("CONTRATO_TERMO");
        assertThat(resultado.tipoContratoDescricao()).isEqualTo("Contrato a termo");
        assertThat(resultado.estado()).isEqualTo("A");
        assertThat(resultado.estadoDescricao()).isEqualTo("Ativo");
        assertThat(resultado.dataInicioPrevisto()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(resultado.dataFimPrevisto()).isEqualTo(LocalDate.of(2027, 2, 28));
        assertThat(resultado.duracaoContrato()).isEqualTo(6);
        assertThat(resultado.contratoUrl()).isNull();
        verify(documentService, never()).gerarLinkPublico(null);
    }

    @Test
    void deveOcultarColocacaoQueNaoPertenceAoCandidato() {
        when(colocacaoRepository.buscarPorId(31, 9001L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(31, 9001L))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(ex.getReason()).contains("não pertence ao candidato");
                });

        verify(colocacaoRepository).buscarPorId(31, 9001L);
    }

    @Test
    void deveRejeitarPessoaOuColocacaoInvalidaAntesDeConsultarBase() {
        assertThatThrownBy(() -> service.listar(null))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex ->
                        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
        assertThatThrownBy(() -> service.buscarPorId(0, 9001L))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex ->
                        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));

        verify(colocacaoRepository, never()).listar(null);
        verify(colocacaoRepository, never()).buscarPorId(0, 9001L);
    }

    private ColocacaoRegisto registo(
            Integer colocacaoId,
            String tipoOferta,
            String tipoContrato,
            String estado,
            String contratoPath
    ) {
        return new ColocacaoRegisto(
                colocacaoId,
                501,
                tipoOferta,
                "Estágio em desenvolvimento",
                "REF-EST-2026-01",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2027, 2, 28),
                tipoContrato,
                6,
                "Colocação para estágio profissional.",
                estado,
                LocalDateTime.of(2026, 8, 27, 10, 30),
                contratoPath
        );
    }
}
