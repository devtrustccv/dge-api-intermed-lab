package cv.dge.dge_api_intermed_lab.web.perfilentidade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.AssiduidadeEstagiarioFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.service.GestaoAssiduidadeService;
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
    void deveEncaminharFiltroPorNomeDoEstagiario() throws Exception {
        when(service.listar(any())).thenReturn(List.of());

        mockMvc.perform(get("/v1/assiduidades")
                        .param("entidadeId", "40")
                        .param("estagiario", "Joao"))
                .andExpect(status().isOk());

        ArgumentCaptor<AssiduidadeEstagiarioFiltro> captor =
                ArgumentCaptor.forClass(AssiduidadeEstagiarioFiltro.class);
        verify(service).listar(captor.capture());
        assertEquals("Joao", captor.getValue().estagiario());
    }
}
