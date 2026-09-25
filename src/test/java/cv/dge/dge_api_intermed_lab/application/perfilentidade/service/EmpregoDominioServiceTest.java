package cv.dge.dge_api_intermed_lab.application.perfilentidade.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.EmpregoDominioResponse;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilentidade.repository.IgrpDominioRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

class EmpregoDominioServiceTest {

    private IgrpDominioRepository repository;
    private EmpregoDominioService service;

    @BeforeEach
    void setUp() {
        repository = mock(IgrpDominioRepository.class);
        service = new EmpregoDominioService(repository);
        ReflectionTestUtils.setField(service, "dad", "interm_laboral");
    }

    @Test
    void deveAceitarValorNovoQueExisteApenasNoIgrp() {
        EmpregoDominioResponse formacaoProfissional = registo(
                900,
                "Formação Profissional",
                "FORMACAO_PROFISSIONAL",
                "ATIVE"
        );
        when(repository.listarPorDominio("HABILITACAO_LITERARIA", "interm_laboral"))
                .thenReturn(List.of(formacaoProfissional));

        assertThat(service.valorOficial("HABILITACAO_LITERARIA", "Formação Profissional"))
                .contains("FORMACAO_PROFISSIONAL");
        assertThat(service.descricao("HABILITACAO_LITERARIA", "FORMACAO_PROFISSIONAL"))
                .isEqualTo("Formação Profissional");
    }

    @Test
    void deveDevolverTodosOsCamposDaTabelaDomain() {
        EmpregoDominioResponse registo = registo(452, "Bacharel", "BACH", "ATIVE");
        when(repository.listarPorDominio("HABILITACAO_LITERARIA", "interm_laboral"))
                .thenReturn(List.of(registo));

        assertThat(service.consultarTodosCampos("HABILITACAO_LITERARIA"))
                .singleElement()
                .isEqualTo(registo);
    }

    @Test
    void deveExcluirValoresInativosDasOpcoesEValidacoes() {
        when(repository.listarPorDominio("HABILITACAO_LITERARIA", "interm_laboral"))
                .thenReturn(List.of(
                        registo(452, "Bacharel", "BACH", "ATIVE"),
                        registo(999, "Opção removida", "REMOVIDA", "INATIVE")
                ));

        assertThat(service.listarPorDominio("HABILITACAO_LITERARIA"))
                .extracting(EmpregoDominioResponse::valor)
                .containsExactly("BACH");
        assertThat(service.valorOficial("HABILITACAO_LITERARIA", "REMOVIDA")).isEmpty();
    }

    @Test
    void deveInformarDominioEAplicacaoQuandoNaoExistir() {
        when(repository.listarPorDominio("DOMINIO_NOVO", "interm_laboral"))
                .thenReturn(List.of());

        assertThatThrownBy(() -> service.consultarTodosCampos("DOMINIO_NOVO"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("DOMINIO_NOVO")
                .hasMessageContaining("interm_laboral");
    }

    @Test
    void deveConstruirMensagemComValorRecebidoEOpcoesAtuaisDoIgrp() {
        when(repository.listarPorDominio("HABILITACAO_LITERARIA", "interm_laboral"))
                .thenReturn(List.of(registo(900, "Formação Profissional", "FORMACAO_PROFISSIONAL", "ATIVE")));

        assertThat(service.mensagemValorInvalido(
                "HABILITACAO_LITERARIA",
                "VALOR_DESCONHECIDO",
                "Habilitação mínima"
        ))
                .contains("VALOR_DESCONHECIDO")
                .contains("Habilitação mínima")
                .contains("FORMACAO_PROFISSIONAL (Formação Profissional)");
    }

    private EmpregoDominioResponse registo(Integer id, String descricao, String valor, String status) {
        return new EmpregoDominioResponse(
                id,
                descricao,
                "PRIVATE",
                "HABILITACAO_LITERARIA",
                1,
                status,
                valor,
                13
        );
    }
}
