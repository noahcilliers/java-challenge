package com.challenge.api.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** The rule that the date must not precede the hire date needs the stored Employee, so EmployeeServiceTest covers it. */
class TerminationDateRequestTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void createValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void acceptsARequestWithADate() {
        TerminationDateRequest request = new TerminationDateRequest();
        request.setContractTerminationDate(Instant.parse("2025-06-30T00:00:00Z"));

        assertThat(invalidAttributes(request)).isEmpty();
    }

    @Test
    void rejectsARequestWithoutADate() {
        assertThat(invalidAttributes(new TerminationDateRequest())).containsExactly("contractTerminationDate");
    }

    private static Set<String> invalidAttributes(TerminationDateRequest request) {
        return validator.validate(request).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }
}
