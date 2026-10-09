package cv.dge.dge_api_intermed_lab.web.perfilcandidato;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaEntrevistaListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.service.MinhaEntrevistaService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class PerfilCandidatoEntrevistaControllerTest {

    @Mock
    private MinhaEntrevistaService entrevistaService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PerfilCandidatoEntrevistaController(entrevistaService))
                .setControllerAdvice(new PerfilCandidatoApiExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void deveListarEntrevistasDoCandidato() throws Exception {
        when(entrevistaService.listar(9001L)).thenReturn(List.of(new MinhaEntrevistaListaResponse(
                25,
                12,
                LocalDate.of(2026, 10, 8),
                LocalDate.of(2026, 10, 15),
                LocalTime.of(9, 0),
                LocalTime.of(9, 30),
                "Técnico Exemplo",
                "Sala 2",
                "AGENDADA",
                4,
                "CEFP Praia",
                "ORIENTACAO_PROFISSIONAL",
                "PRESENCIAL",
                "CEFP Praia"
        )));

        mockMvc.perform(get("/v1/perfil-candidato/entrevistas")
                        .param("pessoaId", "9001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados[0].entrevistaId").value(25))
                .andExpect(jsonPath("$.dados[0].dataEncaminhamento").value("2026-10-08"))
                .andExpect(jsonPath("$.dados[0].dataEntrevista").value("2026-10-15"));

        verify(entrevistaService).listar(9001L);
    }

    @Test
    void deveDevolverErroPadronizadoSemPessoaId() throws Exception {
        String mensagem = "Não foi possível identificar o candidato. Atualize a página, entre novamente e tente de novo.";
        when(entrevistaService.listar(null))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem));

        mockMvc.perform(get("/v1/perfil-candidato/entrevistas"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.sucesso").value(false))
                .andExpect(jsonPath("$.mensagem").value(mensagem))
                .andExpect(jsonPath("$.dados").isEmpty())
                .andExpect(jsonPath("$.erros[0]").value(mensagem));

        verify(entrevistaService).listar(null);
    }
}
