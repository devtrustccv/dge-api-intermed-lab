package cv.dge.dge_api_intermed_lab.web.perfilentidade;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.avaliacao.AvaliacaoEstagiarioFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.avaliacao.AvaliacaoEstagiarioDetalheResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.avaliacao.AvaliacaoEstagiarioListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.avaliacao.AvaliacaoEstagiarioRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.service.GestaoAvaliacaoEstagiarioService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
class GestaoAvaliacaoEstagiarioControllerTest {

    @Mock
    private GestaoAvaliacaoEstagiarioService service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new GestaoAvaliacaoEstagiarioController(service))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void deveAceitarNomeEstagiarioComoFiltroPorNome() throws Exception {
        when(service.listar(any())).thenReturn(List.of());

        mockMvc.perform(get("/v1/avaliacoes-estagiarios")
                        .param("entidadeId", "23")
                        .param("nomeEstagiario", "Maria Silva"))
                .andExpect(status().isOk());

        ArgumentCaptor<AvaliacaoEstagiarioFiltro> filtro = ArgumentCaptor.forClass(AvaliacaoEstagiarioFiltro.class);
        verify(service).listar(filtro.capture());
        assertEquals("Maria Silva", filtro.getValue().estagiario());
    }

    @Test
    void deveIgnorarPessoaIdNaListagem() throws Exception {
        when(service.listar(any())).thenReturn(List.of());

        mockMvc.perform(get("/v1/avaliacoes-estagiarios")
                        .param("entidadeId", "23")
                        .param("pessoaId", "100"))
                .andExpect(status().isOk());

        verify(service).listar(any(AvaliacaoEstagiarioFiltro.class));
    }

    @Test
    void deveDevolverDataRegistoSemHora() throws Exception {
        when(service.listar(any())).thenReturn(List.of(new AvaliacaoEstagiarioListaResponse(
                10,
                100L,
                "Maria Silva",
                "Estágio em Desenvolvimento de Software",
                "MENSAL",
                "Mensal",
                "Agosto 2026",
                BigDecimal.valueOf(16),
                LocalDate.of(2026, 9, 17)
        )));

                mockMvc.perform(get("/v1/avaliacoes-estagiarios")
                        .param("entidadeId", "23"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados[0].oferta").value("Estágio em Desenvolvimento de Software"))
                .andExpect(jsonPath("$.dados[0].dataRegisto").value("2026-09-17"));
    }

    @Test
    void deveDevolverOfertaNoDetalheDaAvaliacao() throws Exception {
        when(service.buscarPorId(10, 23)).thenReturn(new AvaliacaoEstagiarioDetalheResponse(
                10,
                100L,
                "Maria Silva",
                55,
                "Estágio em Desenvolvimento de Software",
                "MENSAL",
                "Mensal",
                "Agosto 2026",
                List.of(),
                "SATISFEITO",
                "Satisfeito",
                "SIM",
                BigDecimal.valueOf(16),
                "Bom desempenho",
                List.of("Reforçar a autonomia"),
                "Desenvolvimento do módulo de relatórios",
                LocalDateTime.of(2026, 9, 17, 10, 30),
                "utilizador@dge.cv",
                null,
                null
        ));

        mockMvc.perform(get("/v1/avaliacoes-estagiarios/10")
                        .param("entidadeId", "23"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.candidaturaId").value(55))
                .andExpect(jsonPath("$.dados.oferta")
                        .value("Estágio em Desenvolvimento de Software"));
    }

    @Test
    void deveReceberSugestaoEDesenvolvimentoAtividadeNoPost() throws Exception {
        mockMvc.perform(post("/v1/avaliacoes-estagiarios")
                        .param("entidadeId", "23")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pessoaId": 100,
                                  "tipoAvaliacao": "MENSAL",
                                  "periodoReferencia": "Outubro 2026",
                                  "avaliacaoDesempenho": [
                                    {"tipoCompetencia": "COMP_TECNICA", "avaliacao": "4"}
                                  ],
                                  "grauSatisfacao": "3",
                                  "classificacao": 4.5,
                                  "observacao": "Bom desempenho",
                                  "sugestao": [
                                    "Reforçar a autonomia",
                                    "Aprofundar os testes automatizados"
                                  ],
                                  "desenvolvimentoAtividade": "Implementação do módulo de relatórios",
                                  "utilizador": "utilizador@dge.cv"
                                }
                                """))
                .andExpect(status().isOk());

        ArgumentCaptor<AvaliacaoEstagiarioRequest> request =
                ArgumentCaptor.forClass(AvaliacaoEstagiarioRequest.class);
        verify(service).criar(eq(23), request.capture());
        assertEquals(
                List.of("Reforçar a autonomia", "Aprofundar os testes automatizados"),
                request.getValue().sugestao()
        );
        assertEquals(
                "Implementação do módulo de relatórios",
                request.getValue().desenvolvimentoAtividade()
        );
    }
}
