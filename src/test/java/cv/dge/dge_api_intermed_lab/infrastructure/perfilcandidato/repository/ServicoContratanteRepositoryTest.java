package cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.ServicoContratanteFiltro;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ServicoContratanteRepositoryTest {

    @Test
    void deveAplicarTodosOsFiltrosDaPrestacaoDeServicos() {
        ServicoContratanteRepository repository = new ServicoContratanteRepository(
                mock(DataSource.class),
                mock(DataSource.class),
                new ObjectMapper()
        );
        List<Object> parametros = new ArrayList<>();

        String where = ReflectionTestUtils.invokeMethod(
                repository,
                "construirWhereServicosContratante",
                new ServicoContratanteFiltro(
                        9001L,
                        "Canalização",
                        "A",
                        LocalDate.of(2026, 9, 1),
                        LocalDate.of(2026, 9, 30)
                ),
                parametros
        );

        assertThat(where)
                .contains("servico.tipo_servico ILIKE ?")
                .contains("servico.estado")
                .contains("servico.inicio_candidatura >= ?")
                .contains("servico.fim_candidatura <= ?");
        assertThat(parametros).containsExactly(
                9001L,
                "%Canalização%",
                "A",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30)
        );
    }
}
