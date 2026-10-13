package cv.dge.dge_api_intermed_lab.web.perfilentidade;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.service.ColocacaoCandidatoService;
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
class ColocacaoCandidatoControllerTest {

    @Mock
    private ColocacaoCandidatoService service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new ColocacaoCandidatoController(service, new ObjectMapper().findAndRegisterModules())
                )
                .setControllerAdvice(new EmpregoApiExceptionHandler())
                .build();
    }

    @Test
    void deveCriarColocacaoComContratoMultipart() throws Exception {
        MockMultipartFile dados = new MockMultipartFile(
                "dados",
                "",
                MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8",
                """
                        {
                          "tipoOferta": "OFERTA_ESTAGIO",
                          "ofertaId": 29,
                          "codigoReferencia": "4/2026",
                          "pessoaId": 126,
                          "tipoContrato": "CONTRATO_TERMO",
                          "duracaoContrato": 6,
                          "dataInicioPrevisto": "2026-10-01",
                          "dataFimPrevisto": "2027-04-01",
                          "descricao": "Colocacao",
                          "utilizador": "utilizador"
                        }
                        """.getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartFile contrato = new MockMultipartFile(
                "documento",
                "contrato.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "contrato".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/v1/colocacoes-candidatos")
                        .file(dados)
                        .file(contrato)
                        .param("entidadeId", "40")
                        .characterEncoding(StandardCharsets.UTF_8.name()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sucesso").value(true));

        verify(service).criar(
                eq(40),
                argThat(request -> request != null
                        && Integer.valueOf(29).equals(request.ofertaId())
                        && Long.valueOf(126).equals(request.pessoaId())
                        && "utilizador".equals(request.utilizador())),
                argThat(ficheiro -> ficheiro != null
                        && "contrato.pdf".equals(ficheiro.getOriginalFilename()))
        );
    }
}
