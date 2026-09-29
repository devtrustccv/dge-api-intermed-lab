package cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.MinhaAssiduidadeFiltro;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class MinhaAssiduidadeRepositoryTest {

    @Test
    void deveFiltrarPelaDataDaAssiduidade() {
        MinhaAssiduidadeRepository repository = new MinhaAssiduidadeRepository(mock(DataSource.class));
        List<Object> parametros = new ArrayList<>();

        String where = ReflectionTestUtils.invokeMethod(
                repository,
                "construirWhere",
                new MinhaAssiduidadeFiltro(
                        9001L,
                        null,
                        null,
                        LocalDate.of(2026, 9, 1),
                        LocalDate.of(2026, 9, 30)
                ),
                parametros
        );

        assertThat(where)
                .contains("assiduidade.data >= ?")
                .contains("assiduidade.data <= ?")
                .doesNotContain("assiduidade.date_create");
        assertThat(parametros).containsExactly(
                9001L,
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30)
        );
    }
}
