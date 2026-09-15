package cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
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
}
