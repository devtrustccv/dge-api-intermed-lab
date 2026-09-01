package cv.dge.dge_api_intermed_lab.web.perfilcandidato;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.service.ServicoCandidatoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class PerfilCandidatoServicoCandidatoControllerTest {

    @Mock
    private ServicoCandidatoService servicoService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new PerfilCandidatoServicoCandidatoController(servicoService)
        ).build();
    }

    @Test
    void deveEncaminharFiltrosDaLista() throws Exception {
        mockMvc.perform(get("/v1/perfil-candidato/prestacoes-servicos/candidato")
                        .param("pessoaId", "2001101700869")
                        .param("tipoServico", "Canalização")
                        .param("estado", "A")
                        .param("dataInicio", "2026-09-01")
                        .param("dataFim", "2026-09-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));

        verify(servicoService).listar(argThat(filtro ->
                filtro.pessoaId().equals(2001101700869L)
                        && "Canalização".equals(filtro.tipoServico())
                        && "A".equals(filtro.estado())
        ));
    }

    @Test
    void deveEncaminharAceitacaoComUtilizador() throws Exception {
        mockMvc.perform(patch("/v1/perfil-candidato/prestacoes-servicos/candidato/41/aceitar")
                        .param("pessoaId", "2001101700869")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"utilizador\":\"candidato.teste\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));

        verify(servicoService).aceitar(41, 2001101700869L, "candidato.teste");
    }

    @Test
    void deveEncaminharRecusaComUtilizador() throws Exception {
        mockMvc.perform(patch("/v1/perfil-candidato/prestacoes-servicos/candidato/41/recusar")
                        .param("pessoaId", "2001101700869")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"utilizador\":\"candidato.teste\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));

        verify(servicoService).recusar(41, 2001101700869L, "candidato.teste");
    }
}
