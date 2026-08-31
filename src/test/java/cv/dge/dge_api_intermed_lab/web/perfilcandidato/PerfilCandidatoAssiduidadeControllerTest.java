package cv.dge.dge_api_intermed_lab.web.perfilcandidato;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.service.MinhaAssiduidadeService;
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
class PerfilCandidatoAssiduidadeControllerTest {

    @Mock
    private MinhaAssiduidadeService assiduidadeService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        PerfilCandidatoAssiduidadeController controller = new PerfilCandidatoAssiduidadeController(
                assiduidadeService,
                new ObjectMapper().findAndRegisterModules()
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void deveCriarAssiduidadeMultipartSemExigirComprovativo() throws Exception {
        MockMultipartFile dados = dados();

        mockMvc.perform(multipart("/v1/perfil-candidato/assiduidades")
                        .file(dados)
                        .characterEncoding(StandardCharsets.UTF_8.name())
                        .param("pessoaId", "9001"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sucesso").value(true));

        verify(assiduidadeService).criar(
                eq(9001L),
                argThat(request -> request != null
                        && "FALTA_JUSTIFICADA".equals(request.tipoAssiduidade())
                        && "Consulta médica.".equals(request.justificacao())),
                isNull()
        );
    }

    @Test
    void deveAceitarNomeDoNovoComprovativoUsadoPeloFrontendNaEdicao() throws Exception {
        MockMultipartFile comprovativo = new MockMultipartFile(
                "novoAnexoComprovativo",
                "Declaração médica.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "pdf".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/v1/perfil-candidato/assiduidades/{assiduidadeId}", 41)
                        .file(dados())
                        .file(comprovativo)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .characterEncoding(StandardCharsets.UTF_8.name())
                        .param("pessoaId", "9001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));

        verify(assiduidadeService).atualizar(
                eq(41),
                eq(9001L),
                argThat(request -> request != null && "kevin@dge.cv".equals(request.utilizador())),
                argThat(ficheiro -> ficheiro != null
                        && "Declaração médica.pdf".equals(ficheiro.getOriginalFilename()))
        );
    }

    private MockMultipartFile dados() {
        return new MockMultipartFile(
                "dados",
                "",
                MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8",
                """
                        {
                          "tipoAssiduidade": "FALTA_JUSTIFICADA",
                          "data": "2026-08-28",
                          "horaEntrada": "08:30",
                          "horaSaida": "17:15",
                          "justificacao": "Consulta médica.",
                          "utilizador": "kevin@dge.cv"
                        }
                        """.getBytes(StandardCharsets.UTF_8)
        );
    }
}
