package cv.dge.dge_api_intermed_lab.web.perfilentidade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.visita.VisitaTecnicaRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.visita.VisitaTecnicaDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.service.GestaoVisitaTecnicaService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class GestaoVisitaTecnicaControllerTest {

    @Mock
    private GestaoVisitaTecnicaService visitaTecnicaService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new GestaoVisitaTecnicaController(visitaTecnicaService))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void deveReceberCandidatoCriterioAvaliacaoEObservacaoNaCriacao() throws Exception {
        when(visitaTecnicaService.criar(eq(40), any())).thenReturn(null);

        mockMvc.perform(post("/v1/visitas-tecnicas")
                        .param("entidadeId", "40")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dataVisita": "2026-10-05",
                                  "visitante": "Técnico do IEFP",
                                  "candidatos": [
                                    {"pessoaId": 126, "nome": "Candidato"}
                                  ],
                                  "horaInicio": "09:00:00",
                                  "horaFim": "10:00:00",
                                  "objetivos": "Acompanhamento",
                                  "cefpId": 7,
                                  "cefp": "CEFP Praia",
                                  "detalhesAvaliacao": [
                                    {
                                      "candidatos": [
                                        {"pessoaId": 126, "nome": "Candidato"}
                                      ],
                                      "criterio": "COMP_TECNICA",
                                      "avaliacao": "4",
                                      "observacao": "Bom desempenho"
                                    }
                                  ],
                                  "utilizador": "utilizador"
                                }
                                """))
                .andExpect(status().isCreated());

        ArgumentCaptor<VisitaTecnicaRequest> captor = ArgumentCaptor.forClass(VisitaTecnicaRequest.class);
        verify(visitaTecnicaService).criar(eq(40), captor.capture());

        var detalhe = captor.getValue().detalhesAvaliacao().get(0);
        assertThat(detalhe.candidatos()).singleElement().satisfies(candidato -> {
            assertThat(candidato.pessoaId()).isEqualTo(126L);
            assertThat(candidato.nome()).isEqualTo("Candidato");
        });
        assertThat(detalhe.criterio()).isEqualTo("COMP_TECNICA");
        assertThat(detalhe.avaliacao()).isEqualTo("4");
        assertThat(detalhe.observacao()).isEqualTo("Bom desempenho");
    }

    @Test
    void detalheDeveExporDataRegistoEUtilizadorDaCriacao() throws Exception {
        LocalDateTime dateCreate = LocalDateTime.of(2026, 11, 24, 10, 15, 30);
        when(visitaTecnicaService.buscarPorId(10, 40)).thenReturn(new VisitaTecnicaDetalheResponse(
                10,
                40,
                LocalDate.of(2026, 11, 25),
                "Técnico IEFP",
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                "09:00 - 10:00",
                "Acompanhamento",
                "CEFP",
                "CEFP",
                7,
                "CEFP Praia",
                "AGENDADO",
                "Agendado",
                List.of(),
                null,
                null,
                null,
                null,
                null,
                List.of(),
                null,
                false,
                true,
                false,
                dateCreate,
                "utilizador@dge.cv",
                null,
                null
        ));

        mockMvc.perform(get("/v1/visitas-tecnicas/10")
                        .param("entidadeId", "40"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.dataRegisto").value("2026-11-24T10:15:30"))
                .andExpect(jsonPath("$.dados.utilizador").value("utilizador@dge.cv"))
                .andExpect(jsonPath("$.dados.dateCreate").value("2026-11-24T10:15:30"))
                .andExpect(jsonPath("$.dados.userCreate").value("utilizador@dge.cv"));
    }
}
