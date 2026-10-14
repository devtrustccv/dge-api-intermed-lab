package cv.dge.dge_api_intermed_lab.infrastructure.perfilentidade.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import cv.dge.dge_api_intermed_lab.application.perfilentidade.dto.colocacao.ColocacaoCandidatoFiltro;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ColocacaoCandidatoRepositoryTest {

    @Test
    void deveAplicarPesquisaGlobalSemDistinguirMaiusculasEMinusculas() {
        ColocacaoCandidatoRepository repository = new ColocacaoCandidatoRepository(mock(DataSource.class));
        List<Object> parametros = new ArrayList<>();

        String where = ReflectionTestUtils.invokeMethod(
                repository,
                "construirWhere",
                new ColocacaoCandidatoFiltro(
                        null, null, null, null, null,
                        null, null, null, 21, "JoÃO"
                ),
                parametros,
                true
        );

        assertThat(where).contains("CONCAT_WS").contains("ILIKE ?");
        assertThat(parametros).containsExactly(21, "%JoÃO%");
    }
}
