package cv.dge.dge_api_intermed_lab.web.perfilcandidato;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.service.MinhaCandidaturaService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class PerfilCandidatoCandidaturaControllerTest {

    @Mock
    private MinhaCandidaturaService candidaturaService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PerfilCandidatoCandidaturaController(candidaturaService))
                .build();
    }

    @Test
    void deveEncaminharEntidadeIlhaEConcelhoParaOFiltro() throws Exception {
        when(candidaturaService.listar(argThat(filtro ->
                "Entidade Exemplo".equals(filtro.entidade())
                        && "Santiago".equals(filtro.ilha())
                        && "Praia".equals(filtro.concelho())
        ))).thenReturn(List.of());

        mockMvc.perform(get("/v1/perfil-candidato/candidaturas")
                        .param("pessoaId", "9001")
                        .param("entidade", "Entidade Exemplo")
                        .param("ilha", "Santiago")
                        .param("concelho", "Praia"))
                .andExpect(status().isOk());

        verify(candidaturaService).listar(argThat(filtro ->
                "Entidade Exemplo".equals(filtro.entidade())
                        && "Santiago".equals(filtro.ilha())
                        && "Praia".equals(filtro.concelho())
        ));
    }
}
