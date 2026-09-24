package cv.dge.dge_api_intermed_lab.web.perfilentidade;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.EmpregoApiResponse;
import cv.dge.dge_api_intermed_lab.web.ApiErrorMessageResolver;
import cv.dge.dge_api_intermed_lab.web.acolhimento.AcolhimentoController;
import cv.dge.dge_api_intermed_lab.web.document.DocumentController;
import cv.dge.dge_api_intermed_lab.web.orientacao.OrientacaoEntrevistaController;
import cv.dge.dge_api_intermed_lab.web.orientacao.OrientacaoServicoController;
import cv.dge.dge_api_intermed_lab.web.pac.PacController;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice(assignableTypes = {
        IntermediacaoLaboralDashboardController.class,
        GestaoVagaController.class,
        CoordenadorOrientadorController.class,
        GestaoCandidaturaController.class,
        ColocacaoCandidatoController.class,
        GestaoAcompanhamentoController.class,
        GestaoAssiduidadeController.class,
        GestaoVisitaTecnicaController.class,
        GestaoAvaliacaoEstagiarioController.class,
        GestaoRelatorioAcompanhamentoController.class,
        AcolhimentoController.class,
        OrientacaoEntrevistaController.class,
        OrientacaoServicoController.class,
        DocumentController.class,
        PacController.class
})
@Slf4j
public class EmpregoApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<EmpregoApiResponse<Void>> tratarResponseStatus(
            ResponseStatusException ex,
            HttpServletRequest request
    ) {
        String mensagem = ex.getReason();
        if (mensagem == null || mensagem.isBlank()) {
            String referencia = ApiErrorMessageResolver.novaReferencia();
            log.error("ResponseStatusException sem motivo. Referência: {}. Operação: {}",
                    referencia, ApiErrorMessageResolver.operacao(request), ex);
            mensagem = ApiErrorMessageResolver.motivoAusente(
                    request,
                    ex.getStatusCode().value(),
                    referencia
            );
        }
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(EmpregoApiResponse.erro(mensagem, List.of(mensagem)));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<EmpregoApiResponse<Void>> tratarParametroObrigatorio(
            MissingServletRequestParameterException ex
    ) {
        String mensagem = ApiErrorMessageResolver.parametroObrigatorio(ex);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(EmpregoApiResponse.erro(mensagem, List.of(mensagem)));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<EmpregoApiResponse<Void>> tratarParteObrigatoria(
            MissingServletRequestPartException ex
    ) {
        String mensagem = ApiErrorMessageResolver.parteObrigatoria(ex);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(EmpregoApiResponse.erro(mensagem, List.of(mensagem)));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<EmpregoApiResponse<Void>> tratarTipoParametro(MethodArgumentTypeMismatchException ex) {
        String mensagem = ApiErrorMessageResolver.parametroInvalido(ex);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(EmpregoApiResponse.erro(mensagem, List.of(mensagem)));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<EmpregoApiResponse<Void>> tratarJsonInvalido(HttpMessageNotReadableException ex) {
        String mensagem = ApiErrorMessageResolver.corpoInvalido(ex, "corpo da requisição");
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(EmpregoApiResponse.erro(mensagem, List.of(mensagem)));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<EmpregoApiResponse<Void>> tratarErroBaseDados(
            DataAccessException ex,
            HttpServletRequest request
    ) {
        String referencia = ApiErrorMessageResolver.novaReferencia();
        log.error("Erro de base de dados. Referência: {}. Operação: {}",
                referencia, ApiErrorMessageResolver.operacao(request), ex);
        String mensagem = ApiErrorMessageResolver.falhaBaseDados(request, referencia);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(EmpregoApiResponse.erro(mensagem, List.of(mensagem)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<EmpregoApiResponse<Void>> tratarErroNaoMapeado(
            Exception ex,
            HttpServletRequest request
    ) {
        String referencia = ApiErrorMessageResolver.novaReferencia();
        log.error("Erro inesperado. Referência: {}. Operação: {}",
                referencia, ApiErrorMessageResolver.operacao(request), ex);
        String mensagem = ApiErrorMessageResolver.falhaInterna(request, referencia);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(EmpregoApiResponse.erro(mensagem, List.of(mensagem)));
    }
}
