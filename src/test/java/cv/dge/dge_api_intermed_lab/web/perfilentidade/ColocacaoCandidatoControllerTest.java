package cv.dge.dge_api_intermed_lab.web.perfilentidade;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.colocacao.ColocacaoCandidatoListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.service.ColocacaoCandidatoService;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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

    @Test
    void deveDevolverContratoNaListaDeColocacoes() throws Exception {
        String link = "https://documentos/document-viewer?path_url=contratos/60.pdf&type=application/pdf";
        when(service.listar(argThat(filtro -> Integer.valueOf(40).equals(filtro.entidadeId()))))
                .thenReturn(List.of(new ColocacaoCandidatoListaResponse(
                        60,
                        "OFERTA_EMPREGO",
                        "Oferta Emprego",
                        "19/2026",
                        126L,
                        "Candidato",
                        "CONTRATO_TERMO",
                        "Contrato a Termo",
                        LocalDate.of(2026, 10, 1),
                        link,
                        LocalDateTime.of(2026, 10, 19, 16, 49)
                )));

        mockMvc.perform(get("/v1/colocacoes-candidatos").param("entidadeId", "40"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados[0].contratoPath").value(link));
    }
}
