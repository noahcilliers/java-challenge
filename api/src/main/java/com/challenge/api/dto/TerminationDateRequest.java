package com.challenge.api.dto;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * The date is required rather than defaulted to "now": the caller knows the real date, which may be backdated or, for
 * a notice period, in the future.
 */
@Getter
@Setter
public class TerminationDateRequest {

    @NotNull private Instant contractTerminationDate;
}
