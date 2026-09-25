package cv.dge.dge_api_intermed_lab.application.perfilentidade.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.dge.dge_api_intermed_lab.application.geografia.service.GlobalGeografiaService;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VagaFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VagaListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VagaRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.constants.EmpregoDominio;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilentidade.repository.GestaoVagaRepository;
import cv.dge.dge_api_intermed_lab.support.EmpregoDominioTestFixture;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class GestaoVagaServiceImplTest {

    @Mock
    private GestaoVagaRepository vagaRepository;

    @Mock
    private GlobalGeografiaService globalGeografiaService;

    private EmpregoDominioService empregoDominioService;
    private GestaoVagaServiceImpl service;

    @BeforeEach
    void setUp() {
        empregoDominioService = EmpregoDominioTestFixture.criar();
        service = new GestaoVagaServiceImpl(
                empregoDominioService,
                vagaRepository,
                globalGeografiaService
        );
    }

    @Test
    void deveFiltrarIlhaEConcelhoPelasDescricoes() {
        when(vagaRepository.listar(any())).thenReturn(List.of(new VagaListaResponse(
                22,
                "Programador",
                "OFERTA_EMPREGO",
                null,
                "1",
                null,
                "11",
                null,
                null,
                2,
                40,
                "Empresa XPTO",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "REF-22",
                "ATIVA",
                null,
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30)
        )));
        when(globalGeografiaService.buscarNomePorCodigo("1")).thenReturn(Optional.of("Santiago"));
        when(globalGeografiaService.buscarNomePorCodigo("11")).thenReturn(Optional.of("Praia"));

        List<VagaListaResponse> resultado = service.listar(new VagaFiltro(
                null,
                40,
                "Empresa XPTO",
                "Santiago",
                "Praia",
                null,
                "REF-22",
                null,
                null,
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30),
                null
        ));

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).ilhaDesc()).isEqualTo("Santiago");
        assertThat(resultado.get(0).concelhoDesc()).isEqualTo("Praia");
        verify(vagaRepository).listar(org.mockito.ArgumentMatchers.argThat(filtro ->
                "Empresa XPTO".equals(filtro.entidade())
                        && filtro.ilha() == null
                        && filtro.concelho() == null
                        && LocalDate.of(2026, 9, 1).equals(filtro.dataInicio())
                        && LocalDate.of(2026, 9, 30).equals(filtro.dataFim())
        ));
    }

    @ParameterizedTest
    @MethodSource("opcoesInvalidas")
    void deveIdentificarCampoEValorDaOpcaoInvalida(
            VagaRequest request,
            String nomeCampo,
            String valorInvalido
    ) {
        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> service.criar(40, request)
        );

        String dominio = dominioDoCampo(nomeCampo);
        assertThat(erro.getReason()).isEqualTo(
                empregoDominioService.mensagemValorInvalido(dominio, valorInvalido, nomeCampo)
        );
    }

    private static Stream<Arguments> opcoesInvalidas() {
        return Stream.of(
                Arguments.of(request("TIPO_INVALIDO", null, null, null, null),
                        "Tipo de oferta", "TIPO_INVALIDO"),
                Arguments.of(request("OFERTA_EMPREGO", "REGIME_INVALIDO", null, null, null),
                        "Regime de contrato", "REGIME_INVALIDO"),
                Arguments.of(request("OFERTA_EMPREGO", null, "HABILITACAO_INVALIDA", null, null),
                        "Habilitação mínima", "HABILITACAO_INVALIDA"),
                Arguments.of(request("OFERTA_EMPREGO", null, null, "NIVEL_INVALIDO", null),
                        "Nível de qualificação", "NIVEL_INVALIDO"),
                Arguments.of(request("OFERTA_EMPREGO", null, null, null, "HABILITACAO_INVALIDA"),
                        "Habilitação máxima", "HABILITACAO_INVALIDA")
        );
    }

    private static String dominioDoCampo(String nomeCampo) {
        return switch (nomeCampo) {
            case "Tipo de oferta" -> EmpregoDominio.DOMINIO_TIPO_OFERTA;
            case "Regime de contrato" -> EmpregoDominio.DOMINIO_REGIME_CONTRATO;
            case "Habilitação mínima", "Habilitação máxima" -> EmpregoDominio.DOMINIO_HABILITACAO_LITERARIA;
            case "Nível de qualificação" -> EmpregoDominio.DOMINIO_NIVEL_QUALIFICACAO;
            default -> throw new IllegalArgumentException("Campo não mapeado no teste: " + nomeCampo);
        };
    }

    private static VagaRequest request(
            String tipoOferta,
            String regimeContrato,
            String habilitacaoMinima,
            String nivelQualificacao,
            String habilitacaoMaxima
    ) {
        return new VagaRequest(
                null,
                tipoOferta,
                "Programador",
                null,
                null,
                null,
                null,
                null,
                regimeContrato,
                null,
                habilitacaoMinima,
                nivelQualificacao,
                1,
                habilitacaoMaxima,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "teste"
        );
    }
}
