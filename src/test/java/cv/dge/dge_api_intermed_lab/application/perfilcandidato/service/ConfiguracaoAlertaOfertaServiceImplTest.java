package cv.dge.dge_api_intermed_lab.application.perfilcandidato.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.dge.dge_api_intermed_lab.application.geografia.service.GlobalGeografiaService;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.AlertaOfertaDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.AlertaOfertaListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.AlertaOfertaOpcoesResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.AlertaOfertaRequest;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ConsultaVagaOpcaoResponse;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.ConfiguracaoAlertaOfertaRepository;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.ConfiguracaoAlertaOfertaRepository.AlertaOfertaDetalheRegisto;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository.ConfiguracaoAlertaOfertaRepository.AlertaOfertaListaRegisto;
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
class ConfiguracaoAlertaOfertaServiceImplTest {

    @Mock
    private ConfiguracaoAlertaOfertaRepository alertaRepository;

    @Mock
    private GlobalGeografiaService globalGeografiaService;

    private ConfiguracaoAlertaOfertaServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ConfiguracaoAlertaOfertaServiceImpl(alertaRepository, globalGeografiaService);
    }

    @Test
    void deveListarConfiguracoesDoCandidatoComDescricoesDosDominios() {
        LocalDateTime dataConfiguracao = LocalDateTime.of(2026, 8, 31, 10, 15);
        when(alertaRepository.listar(9001L)).thenReturn(List.of(new AlertaOfertaListaRegisto(
                51,
                "ESTAGIO",
                "SECUNDARIO",
                "NIVEL_3",
                "ATIVO",
                dataConfiguracao
        )));

        List<AlertaOfertaListaResponse> resultado = service.listar(9001L);

        assertThat(resultado).singleElement().satisfies(item -> {
            assertThat(item.alertaId()).isEqualTo(51);
            assertThat(item.tipoOferta()).isEqualTo("OFERTA_ESTAGIO");
            assertThat(item.tipoOfertaDescricao()).isEqualTo("Oferta estágio");
            assertThat(item.habilitacaoLiteraria()).isEqualTo("ENSINO_SECUNDARIO");
            assertThat(item.habilitacaoLiterariaDescricao()).isEqualTo("Ensino Secundário");
            assertThat(item.nivelQualificacao()).isEqualTo("3");
            assertThat(item.nivelQualificacaoDescricao()).isEqualTo("Nível 3");
            assertThat(item.estado()).isEqualTo("ATIVO");
            assertThat(item.estadoDescricao()).isEqualTo("Ativo");
            assertThat(item.dataConfiguracao()).isEqualTo(dataConfiguracao);
        });
    }

    @Test
    void deveDevolverDetalheComPessoaLocalizacaoEntidadeEAuditoria() {
        when(alertaRepository.buscarPorId(51, 9001L)).thenReturn(Optional.of(registoDetalhe(51)));
        when(alertaRepository.buscarNomePessoa(9001L)).thenReturn(Optional.of("Kevin Sousa"));
        when(alertaRepository.buscarDenominacaoEntidade(17)).thenReturn(Optional.of("Entidade Exemplo"));
        when(globalGeografiaService.buscarNomePorCodigo("11")).thenReturn(Optional.of("Santiago"));
        when(globalGeografiaService.buscarNomePorCodigo("111")).thenReturn(Optional.of("Praia"));

        AlertaOfertaDetalheResponse resultado = service.buscarPorId(51, 9001L);

        assertThat(resultado.pessoaId()).isEqualTo(9001L);
        assertThat(resultado.pessoaNome()).isEqualTo("Kevin Sousa");
        assertThat(resultado.ilhaDescricao()).isEqualTo("Santiago");
        assertThat(resultado.concelhoDescricao()).isEqualTo("Praia");
        assertThat(resultado.entidadeDescricao()).isEqualTo("Entidade Exemplo");
        assertThat(resultado.tipoOfertaDescricao()).isEqualTo("Oferta estágio");
        assertThat(resultado.userCreate()).isEqualTo("kevin@dge.cv");
        assertThat(resultado.userUpdate()).isEqualTo("kevin@dge.cv");
    }

    @Test
    void deveCarregarOpcoesDoFormularioReutilizandoDominiosDoProjeto() {
        when(alertaRepository.buscarNomePessoa(9001L)).thenReturn(Optional.of("Kevin Sousa"));
        when(alertaRepository.listarIlhas()).thenReturn(List.of(
                new ConsultaVagaOpcaoResponse(11L, "11", "Santiago")
        ));
        when(alertaRepository.listarConcelhos("11")).thenReturn(List.of(
                new ConsultaVagaOpcaoResponse(111L, "111", "Praia")
        ));
        when(alertaRepository.listarEntidades()).thenReturn(List.of(
                new ConsultaVagaOpcaoResponse(17L, null, "Entidade Exemplo")
        ));

        AlertaOfertaOpcoesResponse resultado = service.listarOpcoes(9001L, " 11 ");

        assertThat(resultado.pessoaNome()).isEqualTo("Kevin Sousa");
        assertThat(resultado.tiposOferta()).extracting("valor")
                .contains("OFERTA_ESTAGIO", "OFERTA_EMPREGO");
        assertThat(resultado.habilitacoesLiterarias()).isNotEmpty();
        assertThat(resultado.niveisQualificacao()).extracting("valor").contains("2", "3", "4", "5");
        assertThat(resultado.ilhas()).singleElement().extracting(ConsultaVagaOpcaoResponse::descricao)
                .isEqualTo("Santiago");
        assertThat(resultado.concelhos()).singleElement().extracting(ConsultaVagaOpcaoResponse::descricao)
                .isEqualTo("Praia");
        assertThat(resultado.entidades()).singleElement().extracting(ConsultaVagaOpcaoResponse::descricao)
                .isEqualTo("Entidade Exemplo");
    }

    @Test
    void deveCriarConfiguracaoNormalizadaComEstadoAtivo() {
        AlertaOfertaRequest request = new AlertaOfertaRequest(
                "ESTAGIO",
                "11",
                "111",
                17,
                "SECUNDARIO",
                "NIVEL_3",
                " kevin@dge.cv "
        );
        when(alertaRepository.existeIlha("11")).thenReturn(true);
        when(alertaRepository.existeConcelho("111", "11")).thenReturn(true);
        when(alertaRepository.existeEntidade(17)).thenReturn(true);
        when(alertaRepository.inserir(eq(9001L), any(AlertaOfertaRequest.class), eq("ATIVO")))
                .thenReturn(51);
        when(alertaRepository.buscarPorId(51, 9001L)).thenReturn(Optional.of(registoDetalhe(51)));
        when(alertaRepository.buscarNomePessoa(9001L)).thenReturn(Optional.of("Kevin Sousa"));
        when(alertaRepository.buscarDenominacaoEntidade(17)).thenReturn(Optional.of("Entidade Exemplo"));
        when(globalGeografiaService.buscarNomePorCodigo("11")).thenReturn(Optional.of("Santiago"));
        when(globalGeografiaService.buscarNomePorCodigo("111")).thenReturn(Optional.of("Praia"));

        AlertaOfertaDetalheResponse resultado = service.criar(9001L, request);

        ArgumentCaptor<AlertaOfertaRequest> dados = ArgumentCaptor.forClass(AlertaOfertaRequest.class);
        verify(alertaRepository).inserir(eq(9001L), dados.capture(), eq("ATIVO"));
        assertThat(dados.getValue().tipoOferta()).isEqualTo("OFERTA_ESTAGIO");
        assertThat(dados.getValue().habilitacaoLiteraria()).isEqualTo("ENSINO_SECUNDARIO");
        assertThat(dados.getValue().nivelQualificacao()).isEqualTo("3");
        assertThat(dados.getValue().utilizador()).isEqualTo("kevin@dge.cv");
        assertThat(resultado.alertaId()).isEqualTo(51);
    }

    @Test
    void deveOcultarConfiguracaoDeOutroCandidatoNaConsultaEAtualizacao() {
        when(alertaRepository.buscarPorId(51, 9001L)).thenReturn(Optional.empty());
        when(alertaRepository.atualizar(eq(51), eq(9001L), any(AlertaOfertaRequest.class)))
                .thenReturn(false);

        assertThatThrownBy(() -> service.buscarPorId(51, 9001L))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(ex.getReason()).contains("não pertence ao candidato");
                });
        assertThatThrownBy(() -> service.atualizar(
                51,
                9001L,
                new AlertaOfertaRequest(null, null, null, null, null, null, "kevin@dge.cv")
        )).isInstanceOfSatisfying(ResponseStatusException.class, ex ->
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void deveRejeitarDominioOuHierarquiaGeograficaInvalidaAntesDeGravar() {
        assertThatThrownBy(() -> service.criar(
                9001L,
                new AlertaOfertaRequest(
                        "TIPO_INEXISTENTE",
                        null,
                        null,
                        null,
                        null,
                        null,
                        "kevin@dge.cv"
                )
        )).isInstanceOfSatisfying(ResponseStatusException.class, ex ->
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> service.criar(
                9001L,
                new AlertaOfertaRequest(null, null, "111", null, null, null, "kevin@dge.cv")
        )).isInstanceOfSatisfying(ResponseStatusException.class, ex ->
                assertThat(ex.getReason()).contains("Selecione primeiro a ilha"));

        verify(alertaRepository, never()).inserir(eq(9001L), any(), any());
    }

    private AlertaOfertaDetalheRegisto registoDetalhe(Integer alertaId) {
        return new AlertaOfertaDetalheRegisto(
                alertaId,
                9001L,
                "OFERTA_ESTAGIO",
                "11",
                "111",
                17,
                "ENSINO_SECUNDARIO",
                "3",
                "ATIVO",
                LocalDateTime.of(2026, 8, 31, 10, 15),
                "kevin@dge.cv",
                LocalDateTime.of(2026, 8, 31, 11, 45),
                "kevin@dge.cv"
        );
    }
}
