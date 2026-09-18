package cv.dge.dge_api_intermed_lab.configurations;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

class JacksonDateConfigurationTest {

    @Test
    void deveSerializarLocalDateNoFormatoDiaMesAno() throws Exception {
        Jackson2ObjectMapperBuilder builder = Jackson2ObjectMapperBuilder.json();
        new JacksonDateConfiguration().localDateApiCustomizer().customize(builder);
        ObjectMapper objectMapper = builder.build();

        assertThat(objectMapper.writeValueAsString(LocalDate.of(2026, 9, 18)))
                .isEqualTo("\"18/09/2026\"");
    }
}
