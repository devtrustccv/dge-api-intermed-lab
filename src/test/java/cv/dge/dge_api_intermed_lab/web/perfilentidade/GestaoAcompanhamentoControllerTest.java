package cv.dge.dge_api_intermed_lab.web.perfilentidade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.AcompanhamentoEstagiarioFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.service.GestaoAcompanhamentoService;
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
class GestaoAcompanhamentoControllerTest {

    @Mock
    private GestaoAcompanhamentoService service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new GestaoAcompanhamentoController(service))
                .build();
    }

    @Test
    void deveEncaminharFiltrosPorNomeDoEstagiarioEDaOferta() throws Exception {
        when(service.listarEstagiariosSelecionados(any())).thenReturn(List.of());

        mockMvc.perform(get("/v1/acompanhamentos/estagiarios-selecionados")
                        .param("entidadeId", "40")
                        .param("estagiario", "Joao")
                        .param("oferta", "Estagio Informatica"))
                .andExpect(status().isOk());

        ArgumentCaptor<AcompanhamentoEstagiarioFiltro> captor =
                ArgumentCaptor.forClass(AcompanhamentoEstagiarioFiltro.class);
        verify(service).listarEstagiariosSelecionados(captor.capture());
        assertEquals("Joao", captor.getValue().estagiario());
        assertEquals("Estagio Informatica", captor.getValue().oferta());
    }
}
