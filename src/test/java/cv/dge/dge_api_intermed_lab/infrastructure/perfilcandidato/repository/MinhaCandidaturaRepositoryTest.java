package cv.dge.dge_api_intermed_lab.infrastructure.perfilcandidato.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import cv.dge.dge_api_intermed_lab.application.perfilcandidato.dto.candidatura.MinhaCandidaturaFiltro;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class MinhaCandidaturaRepositoryTest {

    @Test
    void deveLerAnexosHistoricosGuardadosComoJsonDuplamenteCodificadoOuPathSimples() {
        MinhaCandidaturaRepository repository = new MinhaCandidaturaRepository(
                mock(DataSource.class),
                new ObjectMapper()
        );

        JsonNode jsonCodificado = ReflectionTestUtils.invokeMethod(
                repository,
                "lerJson",
                "\"{\\\"anexo\\\":\\\"candidaturas/77/cv.pdf\\\"}\""
        );
        JsonNode pathSimples = ReflectionTestUtils.invokeMethod(
                repository,
                "lerJson",
                "candidaturas/77/cv-antigo.pdf"
        );

        assertThat(jsonCodificado.path("anexo").asText()).isEqualTo("candidaturas/77/cv.pdf");
        assertThat(pathSimples.isTextual()).isTrue();
        assertThat(pathSimples.asText()).isEqualTo("candidaturas/77/cv-antigo.pdf");
    }

    @Test
    void deveFiltrarEntidadePeloIdentificadorOuNome() {
        MinhaCandidaturaRepository repository = new MinhaCandidaturaRepository(
                mock(DataSource.class),
                new ObjectMapper()
        );
        List<Object> parametros = new ArrayList<>();

        String where = ReflectionTestUtils.invokeMethod(
                repository,
                "construirWhere",
                new MinhaCandidaturaFiltro(
                        9001L,
                        null,
                        null,
                        "Entidade Exemplo",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                ),
                parametros
        );

        assertThat(where)
                .contains("CAST(COALESCE(c.entidade_id, o.entidade_id) AS VARCHAR) = ?")
                .contains("o.denominacao_entidade ILIKE ?");
        assertThat(parametros).containsExactly(9001L, "Entidade Exemplo", "%Entidade Exemplo%");
    }

    @Test
    void deveAplicarPesquisaGlobalSemDistinguirMaiusculasEMinusculas() {
        MinhaCandidaturaRepository repository = new MinhaCandidaturaRepository(
                mock(DataSource.class),
                new ObjectMapper()
        );
        List<Object> parametros = new ArrayList<>();

        String where = ReflectionTestUtils.invokeMethod(
                repository,
                "construirWhere",
                new MinhaCandidaturaFiltro(
                        9001L, null, null, null, null, null,
                        null, null, null, null, "EmPrEgO"
                ),
                parametros
        );

        assertThat(where).contains("CONCAT_WS").contains("ILIKE ?");
        assertThat(parametros).containsExactly(9001L, "%EmPrEgO%");
    }
}
