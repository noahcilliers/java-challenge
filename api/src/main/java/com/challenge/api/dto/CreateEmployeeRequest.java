package com.challenge.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * The attributes a caller may supply when creating an Employee. Server-owned attributes (uuid, fullName) are absent on
 * purpose, so a caller cannot choose them. No toString is generated, so request contents cannot end up in logs.
 */
@Getter
@Setter
public class CreateEmployeeRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotNull @PositiveOrZero
    private Integer salary;

    @NotNull @Positive private Integer age;

    @NotBlank
    private String jobTitle;

    @NotBlank
    @Email
    private String email;

    @NotNull private Instant contractHireDate;

    /** Optional: null means the Employee has not been terminated. */
    private Instant contractTerminationDate;

    @JsonIgnore
    @AssertTrue(message = "contractTerminationDate must not be before contractHireDate")
    public boolean isContractTerminationDateValid() {
        if (contractHireDate == null || contractTerminationDate == null) {
            return true;
        }
        return !contractTerminationDate.isBefore(contractHireDate);
    }
}
