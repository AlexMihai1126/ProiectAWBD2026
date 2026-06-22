package ro.fmi.awbd.config;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class DateTimeHelperTest {

    private final DateTimeHelper helper = new DateTimeHelper();

    @Test
    void formatsUtcInstantAsBucharestLocalTime() {
        OffsetDateTime utc = OffsetDateTime.parse("2026-06-22T12:00:00Z");

        assertThat(helper.formatInput(utc)).isEqualTo("2026-06-22T15:00");
        assertThat(helper.formatDisplay(utc)).isEqualTo("2026-06-22 15:00");
    }

    @Test
    void inputRoundTripPreservesTheInstant() {
        OffsetDateTime stored = OffsetDateTime.parse("2026-06-22T12:00:00Z");

        OffsetDateTime parsed = helper.parse(helper.formatInput(stored));

        assertThat(parsed.toInstant()).isEqualTo(stored.toInstant());
        assertThat(parsed.getOffset().toString()).isEqualTo("+03:00");
    }

    @Test
    void parsingUsesWinterDaylightSavingOffset() {
        OffsetDateTime parsed = helper.parse("2026-01-22T15:00");

        assertThat(parsed.getOffset().toString()).isEqualTo("+02:00");
        assertThat(parsed.toInstant()).isEqualTo(OffsetDateTime.parse("2026-01-22T13:00:00Z").toInstant());
    }
}
