package cv.dge.dge_api_intermed_lab.web.perfilentidade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.AvaliacaoEstagiarioFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.AvaliacaoEstagiarioListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.service.GestaoAvaliacaoEstagiarioService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class GestaoAvaliacaoEstagiarioControllerTest {

    @Mock
    private GestaoAvaliacaoEstagiarioService service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new GestaoAvaliacaoEstagiarioController(service))
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
    void deveDevolverDataRegistoSemHora() throws Exception {
        when(service.listar(any())).thenReturn(List.of(new AvaliacaoEstagiarioListaResponse(
                10,
                100L,
                "Maria Silva",
                "MENSAL",
                "Mensal",
                "Agosto 2026",
                BigDecimal.valueOf(16),
                LocalDate.of(2026, 9, 17)
        )));

        mockMvc.perform(get("/v1/avaliacoes-estagiarios")
                        .param("entidadeId", "23"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados[0].dataRegisto").value("2026-09-17"));
    }
}
