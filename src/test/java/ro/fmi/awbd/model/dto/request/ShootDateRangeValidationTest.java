package ro.fmi.awbd.model.dto.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ShootDateRangeValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private final OffsetDateTime start = OffsetDateTime.parse("2026-06-22T12:00:00Z");

    @Test
    void createRequestRejectsEndBeforeStart() {
        ShootCreateRequest request = ShootCreateRequest.builder()
                .title("Invalid range")
                .startAt(start)
                .endAt(start.minusMinutes(1))
                .ownerId(1L)
                .locationId(1L)
                .clientId(1L)
                .build();

        assertThat(validator.validate(request))
                .anyMatch(v -> v.getPropertyPath().toString().equals("dateRangeValid"));
    }

    @Test
    void updateRequestRejectsEndBeforeStart() {
        ShootUpdateRequest request = ShootUpdateRequest.builder()
                .startAt(start)
                .endAt(start.minusHours(1))
                .build();

        assertThat(validator.validate(request))
                .anyMatch(v -> v.getPropertyPath().toString().equals("dateRangeValid"));
    }

    @Test
    void equalStartAndEndIsAllowed() {
        ShootCreateRequest request = ShootCreateRequest.builder()
                .title("Zero duration")
                .startAt(start)
                .endAt(start)
                .ownerId(1L)
                .locationId(1L)
                .clientId(1L)
                .build();

        assertThat(validator.validate(request))
                .noneMatch(v -> v.getPropertyPath().toString().equals("dateRangeValid"));
    }
}
