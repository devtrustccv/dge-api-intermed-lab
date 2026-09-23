package cv.dge.dge_api_intermed_lab.application.perfilentidade.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.dge.dge_api_intermed_lab.application.geografia.service.GlobalGeografiaService;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VagaFiltro;
import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.VagaListaResponse;
import cv.dge.dge_api_intermed_lab.infrastructure.perfilentidade.repository.GestaoVagaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GestaoVagaServiceImplTest {

    @Mock
    private GestaoVagaRepository vagaRepository;

    @Mock
    private GlobalGeografiaService globalGeografiaService;

    @InjectMocks
    private GestaoVagaServiceImpl service;

    @Test
    void deveFiltrarIlhaEConcelhoPelasDescricoes() {
        when(vagaRepository.listar(any())).thenReturn(List.of(new VagaListaResponse(
                22,
                "Programador",
                "OFERTA_EMPREGO",
                null,
                "1",
                null,
                "11",
                null,
                null,
                2,
                40,
                "Empresa XPTO",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "REF-22",
                "ATIVA",
                null,
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30)
        )));
        when(globalGeografiaService.buscarNomePorCodigo("1")).thenReturn(Optional.of("Santiago"));
        when(globalGeografiaService.buscarNomePorCodigo("11")).thenReturn(Optional.of("Praia"));

        List<VagaListaResponse> resultado = service.listar(new VagaFiltro(
                null,
                40,
                "Empresa XPTO",
                "Santiago",
                "Praia",
                null,
                "REF-22",
                null,
                null,
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30),
                null
        ));

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).ilhaDesc()).isEqualTo("Santiago");
        assertThat(resultado.get(0).concelhoDesc()).isEqualTo("Praia");
        verify(vagaRepository).listar(org.mockito.ArgumentMatchers.argThat(filtro ->
                "Empresa XPTO".equals(filtro.entidade())
                        && filtro.ilha() == null
                        && filtro.concelho() == null
                        && LocalDate.of(2026, 9, 1).equals(filtro.dataInicio())
                        && LocalDate.of(2026, 9, 30).equals(filtro.dataFim())
        ));
    }
}
