package cv.dge.dge_api_intermed_lab.configurations;

import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import java.time.format.DateTimeFormatter;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonDateConfiguration {

    public static final DateTimeFormatter API_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer localDateApiCustomizer() {
        return builder -> builder.serializers(new LocalDateSerializer(API_DATE_FORMATTER));
    }
}
