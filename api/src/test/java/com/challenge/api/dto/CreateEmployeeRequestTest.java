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

class CreateEmployeeRequestTest {

    private static final Instant HIRE_DATE = Instant.parse("2024-01-15T00:00:00Z");

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
    void acceptsARequestWithEveryRequiredAttribute() {
        assertThat(invalidAttributes(validRequest())).isEmpty();
    }

    @Test
    void rejectsARequestWithNoAttributes() {
        assertThat(invalidAttributes(new CreateEmployeeRequest()))
                .containsExactlyInAnyOrder(
                        "firstName", "lastName", "salary", "age", "jobTitle", "email", "contractHireDate");
    }

    @Test
    void rejectsBlankText() {
        CreateEmployeeRequest request = validRequest();
        request.setFirstName("");
        request.setLastName("   ");

        assertThat(invalidAttributes(request)).containsExactlyInAnyOrder("firstName", "lastName");
    }

    @Test
    void rejectsANegativeSalaryButAcceptsZero() {
        CreateEmployeeRequest request = validRequest();

        request.setSalary(-1);
        assertThat(invalidAttributes(request)).containsExactly("salary");

        request.setSalary(0);
        assertThat(invalidAttributes(request)).isEmpty();
    }

    @Test
    void rejectsAnAgeThatIsNotPositive() {
        CreateEmployeeRequest request = validRequest();
        request.setAge(0);

        assertThat(invalidAttributes(request)).containsExactly("age");
    }

    @Test
    void rejectsAMalformedEmail() {
        CreateEmployeeRequest request = validRequest();
        request.setEmail("not-an-email");

        assertThat(invalidAttributes(request)).containsExactly("email");
    }

    @Test
    void acceptsATerminationDateOnOrAfterTheHireDate() {
        CreateEmployeeRequest request = validRequest();

        request.setContractTerminationDate(HIRE_DATE);
        assertThat(invalidAttributes(request)).isEmpty();

        request.setContractTerminationDate(HIRE_DATE.plusSeconds(86_400));
        assertThat(invalidAttributes(request)).isEmpty();
    }

    @Test
    void rejectsATerminationDateBeforeTheHireDate() {
        CreateEmployeeRequest request = validRequest();
        request.setContractTerminationDate(HIRE_DATE.minusSeconds(1));

        assertThat(invalidAttributes(request)).containsExactly("contractTerminationDateValid");
    }

    private static CreateEmployeeRequest validRequest() {
        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setSalary(85_000);
        request.setAge(34);
        request.setJobTitle("Software Engineer");
        request.setEmail("jane.smith@example.com");
        request.setContractHireDate(HIRE_DATE);
        return request;
    }

    private static Set<String> invalidAttributes(CreateEmployeeRequest request) {
        return validator.validate(request).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }
}
