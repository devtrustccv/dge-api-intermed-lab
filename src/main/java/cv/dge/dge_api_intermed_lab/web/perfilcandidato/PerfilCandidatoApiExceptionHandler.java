package cv.dge.dge_api_intermed_lab.web.perfilcandidato;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.PerfilCandidatoApiResponse;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice(assignableTypes = {
        PerfilCandidatoDashboardController.class,
        PerfilCandidatoAdesaoController.class,
        PerfilCandidatoCertificadoController.class
})
@Slf4j
public class PerfilCandidatoApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<PerfilCandidatoApiResponse<Void>> tratarResponseStatus(ResponseStatusException ex) {
        String mensagem = ex.getReason() == null
                ? "Não foi possível concluir a operação. Atualize a página e tente novamente."
                : ex.getReason();
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(PerfilCandidatoApiResponse.erro(mensagem, List.of(mensagem)));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<PerfilCandidatoApiResponse<Void>> tratarParametroInvalido(
            MethodArgumentTypeMismatchException ex
    ) {
        String mensagem = "Um dos dados informados não é válido. Reveja os dados e tente novamente.";
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(PerfilCandidatoApiResponse.erro(mensagem, List.of(mensagem)));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<PerfilCandidatoApiResponse<Void>> tratarJsonInvalido(
            HttpMessageNotReadableException ex
    ) {
        String mensagem = "Não foi possível interpretar os dados enviados. Reveja os dados e tente novamente.";
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(PerfilCandidatoApiResponse.erro(mensagem, List.of(mensagem)));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<PerfilCandidatoApiResponse<Void>> tratarErroBaseDados(DataAccessException ex) {
        log.error("Erro de base de dados no Perfil Candidato.", ex);
        String mensagem = "Não foi possível concluir a operação neste momento. Tente novamente mais tarde.";
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(PerfilCandidatoApiResponse.erro(mensagem, List.of(mensagem)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<PerfilCandidatoApiResponse<Void>> tratarErroNaoMapeado(Exception ex) {
        log.error("Erro inesperado no Perfil Candidato.", ex);
        String mensagem = "Ocorreu um problema inesperado. Tente novamente mais tarde.";
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(PerfilCandidatoApiResponse.erro(mensagem, List.of(mensagem)));
    }
}
