package cv.dge.dge_api_intermed_lab.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

class ApiErrorMessageResolverTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void deveIdentificarParametroObrigatorioAusente() {
        String mensagem = ApiErrorMessageResolver.parametroObrigatorio(
                new MissingServletRequestParameterException("entidadeId", "Integer")
        );

        assertThat(mensagem)
                .contains("parâmetro obrigatório \"entidadeId\"")
                .contains("número inteiro");
    }

    @Test
    void deveIdentificarParametroComTipoInvalido() {
        MethodArgumentTypeMismatchException erro = new MethodArgumentTypeMismatchException(
                "abc",
                Integer.class,
                "entidadeId",
                null,
                null
        );

        assertThat(ApiErrorMessageResolver.parametroInvalido(erro))
                .contains("valor \"abc\"")
                .contains("parâmetro \"entidadeId\"")
                .contains("número inteiro");
    }

    @Test
    void deveIdentificarCampoEFormatoInvalidosNoJson() {
        JsonProcessingException erro = assertThrows(
                JsonProcessingException.class,
                () -> objectMapper.readValue("{\"dataInicio\":\"24/09/2026\"}", Pedido.class)
        );

        assertThat(ApiErrorMessageResolver.corpoInvalido(erro, "dados da oferta"))
                .contains("valor \"24/09/2026\"")
                .contains("campo \"dataInicio\"")
                .contains("data no formato AAAA-MM-DD");
    }

    @Test
    void deveIdentificarPosicaoDoJsonMalformado() {
        JsonProcessingException erro = assertThrows(
                JsonProcessingException.class,
                () -> objectMapper.readTree("{\"titulo\":}")
        );

        assertThat(ApiErrorMessageResolver.corpoInvalido(erro, "dados da oferta"))
                .contains("JSON enviado em dados da oferta está malformado")
                .contains("linha 1")
                .contains("coluna");
    }

    @Test
    void deveAssociarFalhaInternaAoEndpointEReferencia() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/v1/vagas");

        assertThat(ApiErrorMessageResolver.falhaBaseDados(request, "erro-123"))
                .contains("POST /v1/vagas")
                .contains("base de dados")
                .contains("erro-123");
    }

    private record Pedido(LocalDate dataInicio) {
    }
}
