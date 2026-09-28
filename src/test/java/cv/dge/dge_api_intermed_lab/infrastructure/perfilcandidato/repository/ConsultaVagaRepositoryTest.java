package cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ConsultaVagaFiltro;
import java.util.ArrayList;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ConsultaVagaRepositoryTest {

    @Test
    void deveSepararFiltroDaOrdenacaoNaConsultaDeVagas() {
        ConsultaVagaRepository repository = new ConsultaVagaRepository(
                mock(DataSource.class),
                mock(DataSource.class),
                new ObjectMapper()
        );

        String sql = ReflectionTestUtils.invokeMethod(
                repository,
                "construirSqlListagem",
                " WHERE o.entidade_id = ?"
        );

        assertThat(sql)
                .contains("o.entidade_id = ?\nORDER BY")
                .doesNotContain("?ORDER BY");
    }

    @Test
    void deveExcluirDaListagemOfertasComDataFimUltrapassada() {
        ConsultaVagaRepository repository = new ConsultaVagaRepository(
                mock(DataSource.class),
                mock(DataSource.class),
                new ObjectMapper()
        );

        String where = ReflectionTestUtils.invokeMethod(
                repository,
                "construirWhere",
                new ConsultaVagaFiltro(null, null, null, null, null, null, null, null, null, null),
                new ArrayList<>()
        );

        assertThat(where).contains("o.data_fim_candidatura >= CURRENT_DATE");
    }
}
