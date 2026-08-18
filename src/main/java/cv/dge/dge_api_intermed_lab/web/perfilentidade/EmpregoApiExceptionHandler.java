package cv.dge.dge_api_intermed_lab.web.perfilentidade;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.EmpregoApiResponse;
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
        GestaoRelatorioAcompanhamentoController.class
})
@Slf4j
public class EmpregoApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<EmpregoApiResponse<Void>> tratarResponseStatus(ResponseStatusException ex) {
        String mensagem = ex.getReason() == null
                ? "Não foi possível concluir a operação. Reveja os dados e tente novamente."
                : ex.getReason();
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(EmpregoApiResponse.erro(mensagem, List.of(mensagem)));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<EmpregoApiResponse<Void>> tratarParametroObrigatorio(
            MissingServletRequestParameterException ex
    ) {
        String mensagem = "Falta informação necessária para concluir a operação. "
                + "Atualize a página, preencha os campos obrigatórios e tente novamente.";
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(EmpregoApiResponse.erro(mensagem, List.of(mensagem)));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<EmpregoApiResponse<Void>> tratarTipoParametro(MethodArgumentTypeMismatchException ex) {
        String mensagem = "Um dos dados informados não está no formato esperado. "
                + "Reveja os valores e tente novamente.";
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(EmpregoApiResponse.erro(mensagem, List.of(mensagem)));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<EmpregoApiResponse<Void>> tratarJsonInvalido(HttpMessageNotReadableException ex) {
        String mensagem = "Não foi possível interpretar os dados enviados. "
                + "Atualize a página e tente novamente.";
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(EmpregoApiResponse.erro(mensagem, List.of(mensagem)));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<EmpregoApiResponse<Void>> tratarErroBaseDados(DataAccessException ex) {
        log.error("Erro de base de dados nos endpoints de emprego.", ex);
        String mensagem = "Não foi possível consultar ou guardar as informações neste momento. "
                + "Tente novamente mais tarde.";
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(EmpregoApiResponse.erro(mensagem, List.of(mensagem)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<EmpregoApiResponse<Void>> tratarErroNaoMapeado(Exception ex) {
        log.error("Erro inesperado nos endpoints de emprego.", ex);
        String mensagem = "Ocorreu um problema inesperado ao concluir a operação. "
                + "Tente novamente mais tarde.";
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(EmpregoApiResponse.erro(mensagem, List.of(mensagem)));
    }
}
