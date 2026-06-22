package ro.fmi.awbd.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.OffsetDateTime;

// Lets datetime-local form inputs (no zone offset) bind to OffsetDateTime fields.
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final DateTimeHelper dateTimeHelper;

    public WebConfig(DateTimeHelper dateTimeHelper) {
        this.dateTimeHelper = dateTimeHelper;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(String.class, OffsetDateTime.class, dateTimeHelper::parse);
    }
}
