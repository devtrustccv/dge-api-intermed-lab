package cv.dge.dge_api_intermed_lab.web.perfilcandidato;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.PerfilCandidatoApiResponse;
import cv.dge.dge_api_intermed_lab.web.ApiErrorMessageResolver;
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
        PerfilCandidatoDashboardController.class,
        PerfilCandidatoAdesaoController.class,
        PerfilCandidatoCertificadoController.class,
        PerfilCandidatoVagaController.class,
        PerfilCandidatoCandidaturaController.class,
        PerfilCandidatoColocacaoController.class,
        PerfilCandidatoAvaliacaoController.class,
        PerfilCandidatoAlertaOfertaController.class,
        PerfilCandidatoAssiduidadeController.class,
        PerfilCandidatoServicoContratanteController.class,
        PerfilCandidatoServicoCandidatoController.class
})
@Slf4j
public class PerfilCandidatoApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<PerfilCandidatoApiResponse<Void>> tratarResponseStatus(
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
                .body(PerfilCandidatoApiResponse.erro(mensagem, List.of(mensagem)));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<PerfilCandidatoApiResponse<Void>> tratarParametroObrigatorio(
            MissingServletRequestParameterException ex
    ) {
        String mensagem = ApiErrorMessageResolver.parametroObrigatorio(ex);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(PerfilCandidatoApiResponse.erro(mensagem, List.of(mensagem)));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<PerfilCandidatoApiResponse<Void>> tratarParteObrigatoria(
            MissingServletRequestPartException ex
    ) {
        String mensagem = ApiErrorMessageResolver.parteObrigatoria(ex);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(PerfilCandidatoApiResponse.erro(mensagem, List.of(mensagem)));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<PerfilCandidatoApiResponse<Void>> tratarParametroInvalido(
            MethodArgumentTypeMismatchException ex
    ) {
        String mensagem = ApiErrorMessageResolver.parametroInvalido(ex);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(PerfilCandidatoApiResponse.erro(mensagem, List.of(mensagem)));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<PerfilCandidatoApiResponse<Void>> tratarJsonInvalido(
            HttpMessageNotReadableException ex
    ) {
        String mensagem = ApiErrorMessageResolver.corpoInvalido(ex, "corpo da requisição");
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(PerfilCandidatoApiResponse.erro(mensagem, List.of(mensagem)));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<PerfilCandidatoApiResponse<Void>> tratarErroBaseDados(
            DataAccessException ex,
            HttpServletRequest request
    ) {
        String referencia = ApiErrorMessageResolver.novaReferencia();
        log.error("Erro de base de dados no Perfil Candidato. Referência: {}. Operação: {}",
                referencia, ApiErrorMessageResolver.operacao(request), ex);
        String mensagem = ApiErrorMessageResolver.falhaBaseDados(request, referencia);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(PerfilCandidatoApiResponse.erro(mensagem, List.of(mensagem)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<PerfilCandidatoApiResponse<Void>> tratarErroNaoMapeado(
            Exception ex,
            HttpServletRequest request
    ) {
        String referencia = ApiErrorMessageResolver.novaReferencia();
        log.error("Erro inesperado no Perfil Candidato. Referência: {}. Operação: {}",
                referencia, ApiErrorMessageResolver.operacao(request), ex);
        String mensagem = ApiErrorMessageResolver.falhaInterna(request, referencia);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(PerfilCandidatoApiResponse.erro(mensagem, List.of(mensagem)));
    }
}
