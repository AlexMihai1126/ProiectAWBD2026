package ro.fmi.awbd.config;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component("dateTime")
public class DateTimeHelper {

    static final ZoneId APPLICATION_ZONE = ZoneId.of("Europe/Bucharest");
    private static final DateTimeFormatter INPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public OffsetDateTime parse(String source) {
        if (source == null || source.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(source);
        } catch (DateTimeParseException ex) {
            return LocalDateTime.parse(source).atZone(APPLICATION_ZONE).toOffsetDateTime();
        }
    }

    public String formatInput(OffsetDateTime value) {
        return format(value, INPUT_FORMAT);
    }

    public String formatDisplay(OffsetDateTime value) {
        return format(value, DISPLAY_FORMAT);
    }

    private String format(OffsetDateTime value, DateTimeFormatter formatter) {
        if (value == null) {
            return "";
        }
        return value.atZoneSameInstant(APPLICATION_ZONE).format(formatter);
    }
}
