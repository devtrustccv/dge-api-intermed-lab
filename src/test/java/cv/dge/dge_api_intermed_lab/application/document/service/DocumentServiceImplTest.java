package cv.dge.dge_api_intermed_lab.application.document.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import cv.dge.dge_api_intermed_lab.utils.RestClientHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class DocumentServiceImplTest {

    private static final String VISUALIZADOR_ATUAL =
            "https://deploy.devtrust.cv/dge/app/webapps"
                    + "?r=global_dge/Document_viewer/index"
                    + "&dad=global_dge&target=_blank&isPublic=1&lang=pt_PT";

    private DocumentServiceImpl service;

    @BeforeEach
    void configurar() {
        service = new DocumentServiceImpl(mock(RestClientHelper.class));
        ReflectionTestUtils.setField(service, "docOpen", VISUALIZADOR_ATUAL);
    }

    @Test
    void deveGerarLinkNoFormatoAceitePeloVisualizadorIgrp() {
        String path = "interm_laboral/2026/processos/CANDIDATURA/26/curriculo.pdf";

        String link = service.gerarLinkPublico(path);

        assertThat(link)
                .isEqualTo(VISUALIZADOR_ATUAL + "&path_url=" + path + "&type=application/pdf")
                .doesNotContain("%2F");
    }

    @Test
    void deveReconstruirLinkAntigoComAConfiguracaoAtual() {
        String linkAntigo = "http://localhost:8080/dge/app/webapps"
                + "?r=global_dge/Document_viewer/index"
                + "&path_url=interm_laboral%2F2026%2Fdocumentos%2Fanexo.pdf"
                + "&type=application/pdf";

        String link = service.gerarLinkPublico(linkAntigo);

        assertThat(link).isEqualTo(
                VISUALIZADOR_ATUAL
                        + "&path_url=interm_laboral/2026/documentos/anexo.pdf"
                        + "&type=application/pdf"
        );
    }

    @Test
    void deveInformarOTipoRealDoFicheiro() {
        String link = service.gerarLinkPublico("interm_laboral/2026/documentos/foto.png");

        assertThat(link).endsWith("&type=image/png");
    }

    @Test
    void devePreservarUrlExternaQueNaoPertenceAoVisualizador() {
        String urlExterna = "https://ficheiros.exemplo.cv/documentos/contrato.pdf";

        assertThat(service.gerarLinkPublico(urlExterna)).isEqualTo(urlExterna);
    }
}
