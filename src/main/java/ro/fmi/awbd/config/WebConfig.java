package ro.fmi.awbd.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

// Lets datetime-local form inputs (no zone offset) bind to OffsetDateTime fields.
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToOffsetDateTimeConverter());
    }

    static class StringToOffsetDateTimeConverter implements Converter<String, OffsetDateTime> {
        @Override
        public OffsetDateTime convert(String source) {
            if (source.isBlank()) {
                return null;
            }
            try {
                return OffsetDateTime.parse(source);
            } catch (DateTimeParseException ex) {
                return LocalDateTime.parse(source).atOffset(OffsetDateTime.now().getOffset());
            }
        }
    }
}
