package cv.dge.dge_api_intermed_lab.web.perfilentidade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.comum.EmpregoApiResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.vaga.VagaRequest;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.vaga.VagaResponse;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.service.GestaoVagaService;
import org.junit.jupiter.api.Test;

class GestaoVagaControllerTest {

    @Test
    void deveRetornarSucessoComAvisoQuandoOfertaForGravadaSemCefp() {
        GestaoVagaService service = mock(GestaoVagaService.class);
        GestaoVagaController controller = new GestaoVagaController(service);
        VagaRequest request = mock(VagaRequest.class);
        VagaResponse vaga = mock(VagaResponse.class);
        when(vaga.ilha()).thenReturn("Santiago");
        when(vaga.concelho()).thenReturn("Praia");
        when(vaga.idCefp()).thenReturn(null);
        when(service.criar(40, request)).thenReturn(vaga);

        EmpregoApiResponse<VagaResponse> resposta = controller.criar(40, request);

        assertThat(resposta.sucesso()).isTrue();
        assertThat(resposta.dados()).isSameAs(vaga);
        assertThat(resposta.erros()).isEmpty();
        assertThat(resposta.mensagem())
                .isEqualTo("Aviso: não foi encontrado um CEFP parametrizado para a ilha \"Santiago\" "
                        + "e o concelho \"Praia\". A vaga foi gravada sem associação a CEFP.");
    }
}
