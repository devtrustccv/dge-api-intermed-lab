package cv.dge.dge_api_intermed_lab.web.perfilentidade;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.EmpregoDominioResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.service.EmpregoDominioService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class EmpregoDominioControllerTest {

    private EmpregoDominioService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(EmpregoDominioService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new EmpregoDominioController(service))
                .setControllerAdvice(new EmpregoApiExceptionHandler())
                .build();
    }

    @Test
    void deveReceberSomenteONomeDoDominioEDevolverTodosOsCampos() throws Exception {
        when(service.consultarTodosCampos("HABILITACAO_LITERARIA"))
                .thenReturn(List.of(new EmpregoDominioResponse(
                        900,
                        "Formação Profissional",
                        "PRIVATE",
                        "HABILITACAO_LITERARIA",
                        9,
                        "ATIVE",
                        "FORMACAO_PROFISSIONAL",
                        13
                )));

        mockMvc.perform(get("/v1/dominios")
                        .param("dominio", "HABILITACAO_LITERARIA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados[0].id").value(900))
                .andExpect(jsonPath("$.dados[0].description").value("Formação Profissional"))
                .andExpect(jsonPath("$.dados[0].domainType").value("PRIVATE"))
                .andExpect(jsonPath("$.dados[0].dominio").value("HABILITACAO_LITERARIA"))
                .andExpect(jsonPath("$.dados[0].ordem").value(9))
                .andExpect(jsonPath("$.dados[0].status").value("ATIVE"))
                .andExpect(jsonPath("$.dados[0].valor").value("FORMACAO_PROFISSIONAL"))
                .andExpect(jsonPath("$.dados[0].envFk").value(13));
    }

    @Test
    void deveExplicarQuandoONomeDoDominioNaoForEnviado() throws Exception {
        mockMvc.perform(get("/v1/dominios"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.mensagem").value(
                        "O parâmetro obrigatório \"dominio\" não foi informado. Informe um texto."
                ));
    }
}
