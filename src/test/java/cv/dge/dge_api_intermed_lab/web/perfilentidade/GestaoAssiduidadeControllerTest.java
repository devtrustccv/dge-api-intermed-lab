package cv.dge.dge_api_intermed_lab.web.perfilentidade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.AssiduidadeEstagiarioFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.AssiduidadeEstagiarioListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.service.GestaoAssiduidadeService;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class GestaoAssiduidadeControllerTest {

    @Mock
    private GestaoAssiduidadeService service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new GestaoAssiduidadeController(service))
                .build();
    }

    @Test
    void deveEncaminharFiltrosPorNomeDoEstagiarioEDaOferta() throws Exception {
        when(service.listar(any())).thenReturn(List.of());

        mockMvc.perform(get("/v1/assiduidades")
                        .param("entidadeId", "40")
                        .param("estagiario", "Joao")
                        .param("oferta", "Estagio Informatica"))
                .andExpect(status().isOk());

        ArgumentCaptor<AssiduidadeEstagiarioFiltro> captor =
                ArgumentCaptor.forClass(AssiduidadeEstagiarioFiltro.class);
        verify(service).listar(captor.capture());
        assertEquals("Joao", captor.getValue().estagiario());
        assertEquals("Estagio Informatica", captor.getValue().oferta());
    }

    @Test
    void deveRetornarDadosComplementaresNaListagem() throws Exception {
        when(service.listar(any())).thenReturn(List.of(new AssiduidadeEstagiarioListaResponse(
                1,
                2,
                3,
                "Estagio Informatica",
                126L,
                "Joao",
                "FALTA",
                "Falta",
                LocalDate.of(2026, 9, 23),
                LocalTime.of(8, 0),
                LocalTime.of(17, 0),
                "08:00 - 17:00",
                "PENDENTE",
                "Pendente",
                true,
                "Consulta medica",
                "Aguardando validacao",
                "comprovativo.pdf",
                "utilizador.teste",
                LocalDateTime.of(2026, 9, 23, 18, 30)
        )));

        mockMvc.perform(get("/v1/assiduidades").param("entidadeId", "40"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados[0].justificacao").value("Consulta medica"))
                .andExpect(jsonPath("$.dados[0].observacao").value("Aguardando validacao"))
                .andExpect(jsonPath("$.dados[0].comprovativo").value("comprovativo.pdf"))
                .andExpect(jsonPath("$.dados[0].utilizadorRegisto").value("utilizador.teste"))
                .andExpect(jsonPath("$.dados[0].dataRegistro").exists());
    }
}
