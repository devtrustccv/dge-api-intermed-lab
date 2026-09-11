package cv.dge.dge_api_intermed_lab.web.perfilentidade;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.CandidaturaDocumentoResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.CandidaturaListaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.service.GestaoCandidaturaService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class GestaoCandidaturaControllerTest {

    @Mock
    private GestaoCandidaturaService candidaturaService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new GestaoCandidaturaController(candidaturaService))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(
                        new ObjectMapper()
                                .findAndRegisterModules()
                                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                ))
                .build();
    }

    @Test
    void deveDevolverDadosComplementaresNaListaDeCandidaturas() throws Exception {
        CandidaturaDocumentoResponse anexo = new CandidaturaDocumentoResponse(
                "CURRICULO_VITAE",
                "cv.pdf",
                "/candidaturas/1/cv.pdf",
                "http://localhost/documentos?path_url=%2Fcandidaturas%2F1%2Fcv.pdf"
        );
        when(candidaturaService.listar(any())).thenReturn(List.of(new CandidaturaListaResponse(
                1,
                143L,
                "Silvânia Patricia Lopes Correia",
                LocalDate.of(1995, 4, 12),
                "Feminino",
                "silvania@example.cv",
                "9912345",
                "Santiago / Praia",
                "Achada Santo António",
                "LICENCIATURA",
                "OFERTA_EMPREGO",
                "Oferta Emprego",
                22,
                "OF-2026-001",
                "Emprego em Programação",
                "PORTAL",
                "Portal",
                "CURRICULO_VITAE",
                anexo,
                List.of(anexo),
                "TRIAGEM",
                "Triagem",
                null,
                null,
                false,
                false,
                5,
                true,
                LocalDateTime.of(2026, 8, 31, 15, 14, 50)
        )));

        mockMvc.perform(get("/v1/candidaturas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados[0].dataNascCandidato").value("1995-04-12"))
                .andExpect(jsonPath("$.dados[0].sexoCandidato").value("Feminino"))
                .andExpect(jsonPath("$.dados[0].emailCandidato").value("silvania@example.cv"))
                .andExpect(jsonPath("$.dados[0].telefoneCandidato").value("9912345"))
                .andExpect(jsonPath("$.dados[0].ilhaConcelhoCandidato").value("Santiago / Praia"))
                .andExpect(jsonPath("$.dados[0].moradaCandidato").value("Achada Santo António"))
                .andExpect(jsonPath("$.dados[0].habilitacaoLiterariaCandidato").value("LICENCIATURA"))
                .andExpect(jsonPath("$.dados[0].codigoOferta").value("OF-2026-001"))
                .andExpect(jsonPath("$.dados[0].tipoDocumento").value("CURRICULO_VITAE"))
                .andExpect(jsonPath("$.dados[0].anexo.path").value("/candidaturas/1/cv.pdf"))
                .andExpect(jsonPath("$.dados[0].anexos[0].nome").value("cv.pdf"))
                .andExpect(jsonPath("$.dados[0].anexos[0].url")
                        .value("http://localhost/documentos?path_url=%2Fcandidaturas%2F1%2Fcv.pdf"))
                .andExpect(jsonPath("$.dados[0].motivoRecusa").doesNotExist());
    }
}
