package cv.dge.dge_api_intermed_lab.application.orientacao.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cv.dge.dge_api_intermed_lab.application.document.service.DocumentService;
import cv.dge.dge_api_intermed_lab.application.orientacao.dto.OrientacaoServicoResponse;
import cv.dge.dge_api_intermed_lab.domain.orientacao.model.AcolhimentoServico;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class OrientacaoMapperTest {

    @Test
    void deveMontarAnexosAPartirDosDetalhesGuardadosNoServico() {
        DocumentService documentService = mock(DocumentService.class);
        OrientacaoMapper mapper = new OrientacaoMapper(documentService);
        String path = "interm_laboral/2026/modulos/SUB_DESEMP/31/declaracao.pdf";

        AcolhimentoServico servico = new AcolhimentoServico();
        servico.setId(31);
        servico.setDetalhesServico(Map.of(
                "anexos",
                List.of(Map.of(
                        "documento", "8",
                        "documento_desc", "Declaração",
                        "anexo", path
                ))
        ));
        when(documentService.gerarLinkPublico(path))
                .thenReturn("https://documentos/declaracao");

        OrientacaoServicoResponse resposta = mapper.toServicoResponse(servico);

        assertThat(resposta.detalhesServico().get("anexos"))
                .asList()
                .singleElement()
                .isInstanceOfSatisfying(Map.class, anexo -> {
                    assertThat(anexo.get("documento")).isEqualTo("8");
                    assertThat(anexo.get("documento_desc")).isEqualTo("Declaração");
                    assertThat(anexo.get("anexo")).isEqualTo(path);
                    assertThat(anexo.get("ver_documento")).isEqualTo("https://documentos/declaracao");
                });
    }
}
