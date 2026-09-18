package com.challenge.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.challenge.api.dto.CreateEmployeeRequest;
import com.challenge.api.dto.TerminationDateRequest;
import com.challenge.api.exception.EmployeeNotFoundException;
import com.challenge.api.exception.InvalidTerminationDateException;
import com.challenge.api.model.Employee;
import com.challenge.api.repository.EmployeeRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EmployeeServiceTest {

    private static final Instant HIRE_DATE = Instant.parse("2024-01-15T00:00:00Z");

    private EmployeeService employeeService;

    @BeforeEach
    void createServiceWithEmptyStore() {
        employeeService = new EmployeeService(new EmployeeRepository());
    }

    @Test
    void createCopiesEveryRequestAttributeOntoTheEmployee() {
        CreateEmployeeRequest request = validRequest("Jane", "Smith");
        request.setContractTerminationDate(HIRE_DATE.plusSeconds(86_400));

        Employee created = employeeService.createEmployee(request);

        assertThat(created.getFirstName()).isEqualTo("Jane");
        assertThat(created.getLastName()).isEqualTo("Smith");
        assertThat(created.getFullName()).isEqualTo("Jane Smith");
        assertThat(created.getSalary()).isEqualTo(85_000);
        assertThat(created.getAge()).isEqualTo(34);
        assertThat(created.getJobTitle()).isEqualTo("Software Engineer");
        assertThat(created.getEmail()).isEqualTo("jane.smith@example.com");
        assertThat(created.getContractHireDate()).isEqualTo(HIRE_DATE);
        assertThat(created.getContractTerminationDate()).isEqualTo(HIRE_DATE.plusSeconds(86_400));
    }

    @Test
    void createLeavesTerminationDateNullWhenNotSupplied() {
        Employee created = employeeService.createEmployee(validRequest("Jane", "Smith"));

        assertThat(created.getContractTerminationDate()).isNull();
    }

    @Test
    void createAssignsADistinctUuidToEachEmployee() {
        Employee first = employeeService.createEmployee(validRequest("Jane", "Smith"));
        Employee second = employeeService.createEmployee(validRequest("Jane", "Smith"));

        assertThat(first.getUuid()).isNotNull();
        assertThat(second.getUuid()).isNotNull().isNotEqualTo(first.getUuid());
    }

    @Test
    void getByUuidReturnsAPreviouslyCreatedEmployee() {
        Employee created = employeeService.createEmployee(validRequest("Jane", "Smith"));

        assertThat(employeeService.getEmployeeByUuid(created.getUuid())).isSameAs(created);
    }

    @Test
    void getByUuidThrowsWhenNoEmployeeHasThatUuid() {
        UUID unknown = UUID.randomUUID();

        assertThatThrownBy(() -> employeeService.getEmployeeByUuid(unknown))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining(unknown.toString());
    }

    @Test
    void getAllIsEmptyBeforeAnythingIsCreated() {
        assertThat(employeeService.getAllEmployees()).isEmpty();
    }

    @Test
    void getAllReturnsEveryCreatedEmployee() {
        Employee first = employeeService.createEmployee(validRequest("Jane", "Smith"));
        Employee second = employeeService.createEmployee(validRequest("John", "Doe"));

        assertThat(employeeService.getAllEmployees()).containsExactlyInAnyOrder(first, second);
    }

    @Test
    void setTerminationDateStoresTheDateAndKeepsTheEmployee() {
        Employee created = employeeService.createEmployee(validRequest("Jane", "Smith"));

        Employee updated =
                employeeService.setTerminationDate(created.getUuid(), terminationOn(HIRE_DATE.plusSeconds(60)));

        assertThat(updated.getContractTerminationDate()).isEqualTo(HIRE_DATE.plusSeconds(60));
        assertThat(employeeService.getEmployeeByUuid(created.getUuid()).getContractTerminationDate())
                .isEqualTo(HIRE_DATE.plusSeconds(60));
        assertThat(employeeService.getAllEmployees()).containsExactly(created);
    }

    @Test
    void setTerminationDateAcceptsTheHireDateItself() {
        Employee created = employeeService.createEmployee(validRequest("Jane", "Smith"));

        Employee updated = employeeService.setTerminationDate(created.getUuid(), terminationOn(HIRE_DATE));

        assertThat(updated.getContractTerminationDate()).isEqualTo(HIRE_DATE);
    }

    @Test
    void setTerminationDateIsIdempotentAndALaterDateReplacesTheEarlierOne() {
        UUID uuid =
                employeeService.createEmployee(validRequest("Jane", "Smith")).getUuid();

        employeeService.setTerminationDate(uuid, terminationOn(HIRE_DATE.plusSeconds(60)));
        employeeService.setTerminationDate(uuid, terminationOn(HIRE_DATE.plusSeconds(60)));
        assertThat(employeeService.getEmployeeByUuid(uuid).getContractTerminationDate())
                .isEqualTo(HIRE_DATE.plusSeconds(60));

        employeeService.setTerminationDate(uuid, terminationOn(HIRE_DATE.plusSeconds(120)));
        assertThat(employeeService.getEmployeeByUuid(uuid).getContractTerminationDate())
                .isEqualTo(HIRE_DATE.plusSeconds(120));
    }

    @Test
    void setTerminationDateRejectsADateBeforeTheHireDateAndChangesNothing() {
        UUID uuid =
                employeeService.createEmployee(validRequest("Jane", "Smith")).getUuid();

        assertThatThrownBy(() -> employeeService.setTerminationDate(uuid, terminationOn(HIRE_DATE.minusSeconds(1))))
                .isInstanceOf(InvalidTerminationDateException.class);
        assertThat(employeeService.getEmployeeByUuid(uuid).getContractTerminationDate())
                .isNull();
    }

    @Test
    void setTerminationDateThrowsWhenNoEmployeeHasThatUuid() {
        assertThatThrownBy(() -> employeeService.setTerminationDate(UUID.randomUUID(), terminationOn(HIRE_DATE)))
                .isInstanceOf(EmployeeNotFoundException.class);
    }

    private static TerminationDateRequest terminationOn(Instant date) {
        TerminationDateRequest request = new TerminationDateRequest();
        request.setContractTerminationDate(date);
        return request;
    }

    private static CreateEmployeeRequest validRequest(String firstName, String lastName) {
        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setSalary(85_000);
        request.setAge(34);
        request.setJobTitle("Software Engineer");
        request.setEmail((firstName + "." + lastName + "@example.com").toLowerCase());
        request.setContractHireDate(HIRE_DATE);
        return request;
    }
}
