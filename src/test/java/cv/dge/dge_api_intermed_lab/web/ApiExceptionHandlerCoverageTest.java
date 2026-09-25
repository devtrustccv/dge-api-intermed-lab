package cv.dge.dge_api_intermed_lab.web;

import static org.assertj.core.api.Assertions.assertThat;

import cv.dge.dge_api_intermed_lab.web.acolhimento.AcolhimentoController;
import cv.dge.dge_api_intermed_lab.web.document.DocumentController;
import cv.dge.dge_api_intermed_lab.web.orientacao.OrientacaoEntrevistaController;
import cv.dge.dge_api_intermed_lab.web.orientacao.OrientacaoServicoController;
import cv.dge.dge_api_intermed_lab.web.pac.PacController;
import cv.dge.dge_api_intermed_lab.web.perfilcandidato.PerfilCandidatoAdesaoController;
import cv.dge.dge_api_intermed_lab.web.perfilcandidato.PerfilCandidatoAlertaOfertaController;
import cv.dge.dge_api_intermed_lab.web.perfilcandidato.PerfilCandidatoApiExceptionHandler;
import cv.dge.dge_api_intermed_lab.web.perfilcandidato.PerfilCandidatoAssiduidadeController;
import cv.dge.dge_api_intermed_lab.web.perfilcandidato.PerfilCandidatoAvaliacaoController;
import cv.dge.dge_api_intermed_lab.web.perfilcandidato.PerfilCandidatoCandidaturaController;
import cv.dge.dge_api_intermed_lab.web.perfilcandidato.PerfilCandidatoCertificadoController;
import cv.dge.dge_api_intermed_lab.web.perfilcandidato.PerfilCandidatoColocacaoController;
import cv.dge.dge_api_intermed_lab.web.perfilcandidato.PerfilCandidatoDashboardController;
import cv.dge.dge_api_intermed_lab.web.perfilcandidato.PerfilCandidatoServicoCandidatoController;
import cv.dge.dge_api_intermed_lab.web.perfilcandidato.PerfilCandidatoServicoContratanteController;
import cv.dge.dge_api_intermed_lab.web.perfilcandidato.PerfilCandidatoVagaController;
import cv.dge.dge_api_intermed_lab.web.perfilentidade.ColocacaoCandidatoController;
import cv.dge.dge_api_intermed_lab.web.perfilentidade.CoordenadorOrientadorController;
import cv.dge.dge_api_intermed_lab.web.perfilentidade.EmpregoApiExceptionHandler;
import cv.dge.dge_api_intermed_lab.web.perfilentidade.EmpregoDominioController;
import cv.dge.dge_api_intermed_lab.web.perfilentidade.GestaoAcompanhamentoController;
import cv.dge.dge_api_intermed_lab.web.perfilentidade.GestaoAssiduidadeController;
import cv.dge.dge_api_intermed_lab.web.perfilentidade.GestaoAvaliacaoEstagiarioController;
import cv.dge.dge_api_intermed_lab.web.perfilentidade.GestaoCandidaturaController;
import cv.dge.dge_api_intermed_lab.web.perfilentidade.GestaoRelatorioAcompanhamentoController;
import cv.dge.dge_api_intermed_lab.web.perfilentidade.GestaoVagaController;
import cv.dge.dge_api_intermed_lab.web.perfilentidade.GestaoVisitaTecnicaController;
import cv.dge.dge_api_intermed_lab.web.perfilentidade.IntermediacaoLaboralDashboardController;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RestControllerAdvice;

class ApiExceptionHandlerCoverageTest {

    @Test
    void todosOsControllersDevemTerHandlerDeErroEstruturado() {
        Set<Class<?>> controllersCobertos = Stream.concat(
                        tiposDoAdvice(EmpregoApiExceptionHandler.class),
                        tiposDoAdvice(PerfilCandidatoApiExceptionHandler.class)
                )
                .collect(Collectors.toSet());

        assertThat(controllersCobertos).containsExactlyInAnyOrder(
                AcolhimentoController.class,
                DocumentController.class,
                OrientacaoEntrevistaController.class,
                OrientacaoServicoController.class,
                PacController.class,
                PerfilCandidatoAdesaoController.class,
                PerfilCandidatoAlertaOfertaController.class,
                PerfilCandidatoAssiduidadeController.class,
                PerfilCandidatoAvaliacaoController.class,
                PerfilCandidatoCandidaturaController.class,
                PerfilCandidatoCertificadoController.class,
                PerfilCandidatoColocacaoController.class,
                PerfilCandidatoDashboardController.class,
                PerfilCandidatoServicoCandidatoController.class,
                PerfilCandidatoServicoContratanteController.class,
                PerfilCandidatoVagaController.class,
                ColocacaoCandidatoController.class,
                CoordenadorOrientadorController.class,
                EmpregoDominioController.class,
                GestaoAcompanhamentoController.class,
                GestaoAssiduidadeController.class,
                GestaoAvaliacaoEstagiarioController.class,
                GestaoCandidaturaController.class,
                GestaoRelatorioAcompanhamentoController.class,
                GestaoVagaController.class,
                GestaoVisitaTecnicaController.class,
                IntermediacaoLaboralDashboardController.class
        );
    }

    private Stream<Class<?>> tiposDoAdvice(Class<?> tipoAdvice) {
        RestControllerAdvice advice = tipoAdvice.getAnnotation(RestControllerAdvice.class);
        return Arrays.stream(advice.assignableTypes());
    }
}
