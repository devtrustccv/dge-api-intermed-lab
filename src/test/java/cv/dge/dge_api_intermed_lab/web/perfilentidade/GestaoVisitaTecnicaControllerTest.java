package cv.dge.dge_api_intermed_lab.web.perfilentidade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VisitaTecnicaRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.service.GestaoVisitaTecnicaService;
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
        mockMvc = MockMvcBuilders
                .standaloneSetup(new GestaoVisitaTecnicaController(visitaTecnicaService))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(
                        new ObjectMapper().findAndRegisterModules()
                ))
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
}
