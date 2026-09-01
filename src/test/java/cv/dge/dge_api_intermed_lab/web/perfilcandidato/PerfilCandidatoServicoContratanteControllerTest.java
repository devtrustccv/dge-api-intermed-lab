package cv.dge.dge_api_intermed_lab.web.perfilcandidato;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.service.ServicoContratanteService;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class PerfilCandidatoServicoContratanteControllerTest {

    @Mock
    private ServicoContratanteService servicoService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        PerfilCandidatoServicoContratanteController controller =
                new PerfilCandidatoServicoContratanteController(
                        servicoService,
                        new ObjectMapper().findAndRegisterModules()
                );
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void deveCriarServicoJsonSemExigirAnexos() throws Exception {
        mockMvc.perform(post("/v1/perfil-candidato/prestacoes-servicos/contratante")
                        .param("pessoaId", "9001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sucesso").value(true));

        verify(servicoService).criar(
                eq(9001L),
                argThat(request -> request != null
                        && "Canalização".equals(request.tipoServico())
                        && request.valorPrevisto().toPlainString().equals("15000.00")),
                argThat(anexos -> anexos != null && anexos.isEmpty()),
                eq(false)
        );
    }

    @Test
    void deveAceitarMultipartComNomeDocumentosUsadoPeloFrontend() throws Exception {
        MockMultipartFile dados = new MockMultipartFile(
                "dados",
                "",
                MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8",
                dadosJson().getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartFile documento = new MockMultipartFile(
                "documentos",
                "termos.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "pdf".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/v1/perfil-candidato/prestacoes-servicos/contratante")
                        .file(dados)
                        .file(documento)
                        .param("pessoaId", "9001")
                        .characterEncoding(StandardCharsets.UTF_8.name()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sucesso").value(true));

        verify(servicoService).criar(
                eq(9001L),
                argThat(request -> request != null && "Canalização".equals(request.tipoServico())),
                argThat(anexos -> anexos.size() == 1
                        && "termos.pdf".equals(anexos.get(0).getOriginalFilename())),
                eq(false)
        );
    }

    @Test
    void deveCriarRascunhoPeloEndpointEspecifico() throws Exception {
        mockMvc.perform(post("/v1/perfil-candidato/prestacoes-servicos/contratante/rascunho")
                        .param("pessoaId", "9001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sucesso").value(true));

        verify(servicoService).criar(
                eq(9001L),
                argThat(request -> request != null),
                argThat(anexos -> anexos.isEmpty()),
                eq(true)
        );
    }

    private String dadosJson() {
        return """
                {
                  "tipoServico": "Canalização",
                  "titulo": "Reparação da instalação",
                  "descricao": "Reparar uma fuga de água.",
                  "dataPretendida": "2026-10-10",
                  "valorPrevisto": 15000.00,
                  "competenciasExigidas": "Experiência em canalização.",
                  "inicioCandidatura": "2026-09-01",
                  "fimCandidatura": "2026-09-30",
                  "telefone": "+238 999 00 00",
                  "email": "kevin@dge.cv",
                  "utilizador": "kevin@dge.cv"
                }
                """;
    }
}
