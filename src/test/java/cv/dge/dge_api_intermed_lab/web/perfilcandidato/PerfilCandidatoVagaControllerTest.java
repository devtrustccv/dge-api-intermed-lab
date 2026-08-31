package cv.dge.dge_api_intermed_lab.web.perfilcandidato;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.service.ConsultaVagaService;
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
class PerfilCandidatoVagaControllerTest {

    @Mock
    private ConsultaVagaService consultaVagaService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        PerfilCandidatoVagaController controller = new PerfilCandidatoVagaController(
                consultaVagaService,
                new ObjectMapper()
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void deveAceitarNomesMultipartEnviadosPeloFrontend() throws Exception {
        MockMultipartFile dados = new MockMultipartFile(
                "dados",
                "",
                MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8",
                """
                        {
                          "habilitacaoAcademica": "LICENCIATURA",
                          "areaFormacao": "Programação",
                          "utilizador": "kevin@dge.cv"
                        }
                        """.getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartFile curriculo = new MockMultipartFile(
                "cv",
                "Kevin Sousa.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "curriculo".getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartFile documento = new MockMultipartFile(
                "documentos",
                "Diploma.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "diploma".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/v1/perfil-candidato/vagas/{ofertaId}/candidaturas", 22)
                        .file(dados)
                        .file(curriculo)
                        .file(documento)
                        .characterEncoding(StandardCharsets.UTF_8.name())
                        .param("pessoaId", "9001"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sucesso").value(true));

        verify(consultaVagaService).candidatar(
                eq(22),
                eq(9001L),
                argThat(request -> request != null
                        && "LICENCIATURA".equals(request.habilitacaoAcademica())
                        && "Programação".equals(request.areaFormacao())),
                argThat(ficheiro -> ficheiro != null
                        && "Kevin Sousa.pdf".equals(ficheiro.getOriginalFilename())),
                argThat(ficheiros -> ficheiros != null
                        && ficheiros.size() == 1
                        && "Diploma.pdf".equals(ficheiros.get(0).getOriginalFilename()))
        );
    }

    @Test
    void deveAceitarCandidaturaMultipartSemAnexos() throws Exception {
        MockMultipartFile dados = new MockMultipartFile(
                "dados",
                "",
                MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8",
                """
                        {
                          "habilitacaoAcademica": "LICENCIATURA",
                          "areaFormacao": "Programação",
                          "utilizador": "kevin@dge.cv"
                        }
                        """.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/v1/perfil-candidato/vagas/{ofertaId}/candidaturas", 23)
                        .file(dados)
                        .characterEncoding(StandardCharsets.UTF_8.name())
                        .param("pessoaId", "9001"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sucesso").value(true));

        verify(consultaVagaService).candidatar(
                eq(23),
                eq(9001L),
                argThat(request -> request != null
                        && "LICENCIATURA".equals(request.habilitacaoAcademica())),
                isNull(),
                argThat(ficheiros -> ficheiros != null && ficheiros.isEmpty())
        );
    }
}
