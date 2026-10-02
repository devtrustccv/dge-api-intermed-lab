package cv.dge.dge_api_intermed_lab.application.orientacao.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cv.dge.dge_api_intermed_lab.application.document.dto.DocumentoResponseDTO;
import cv.dge.dge_api_intermed_lab.application.document.service.DocumentService;
import cv.dge.dge_api_intermed_lab.application.orientacao.dto.OrientacaoServicoResponse;
import cv.dge.dge_api_intermed_lab.domain.orientacao.model.AcolhimentoServico;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class OrientacaoMapperTest {

    @Test
    void deveMontarAnexosDoDetalheAPartirDaRelacaoDocumental() {
        DocumentService documentService = mock(DocumentService.class);
        OrientacaoMapper mapper = new OrientacaoMapper(documentService);
        ReflectionTestUtils.setField(mapper, "appCodeDocumentoOrientacao", "interm_laboral");
        ReflectionTestUtils.setField(mapper, "tipoRelacaoDocumentoOrientacao", "SUB_DESEMP");

        AcolhimentoServico servico = new AcolhimentoServico();
        servico.setId(31);
        servico.setDetalhesServico(Map.of(
                "anexos",
                List.of(Map.of("anexo", "documentos/legado.pdf"))
        ));

        DocumentoResponseDTO documento = DocumentoResponseDTO.builder()
                .id(51L)
                .idTpDoc("8")
                .name("Declaração")
                .fileName("declaracao")
                .path("interm_laboral/2026/modulos/SUB_DESEMP/31/declaracao.pdf")
                .build();
        when(documentService.getDocumentosPorRelacao(31, "SUB_DESEMP", "interm_laboral"))
                .thenReturn(List.of(documento));
        when(documentService.gerarLinkPublico(documento.getPath()))
                .thenReturn("https://documentos/declaracao");

        OrientacaoServicoResponse resposta = mapper.toServicoResponse(servico);

        assertThat(resposta.detalhesServico().get("anexos"))
                .asList()
                .singleElement()
                .isInstanceOfSatisfying(Map.class, anexo -> {
                    assertThat(anexo.get("documento")).isEqualTo("8");
                    assertThat(anexo.get("documento_desc")).isEqualTo("Declaração");
                    assertThat(anexo.get("anexo")).isEqualTo(documento.getPath());
                    assertThat(anexo.get("ver_documento")).isEqualTo("https://documentos/declaracao");
                });
    }
}
