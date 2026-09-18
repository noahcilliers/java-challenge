package com.challenge.api.config;

import com.challenge.api.dto.CreateEmployeeRequest;
import com.challenge.api.service.EmployeeService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Populates the in-memory store with mock Employees at startup, standing in for the existing employee data a real
 * persistence layer would hold. Goes through the Service so mock Employees are built exactly like real ones.
 */
@Component
@RequiredArgsConstructor
public class MockEmployeeSeeder implements ApplicationRunner {

    private final EmployeeService employeeService;

    @Override
    public void run(ApplicationArguments args) {
        employeeService.createEmployee(
                mockRequest("Ada", "Lovelace", 120_000, 36, "Principal Engineer", "2019-03-01T00:00:00Z", null));
        employeeService.createEmployee(
                mockRequest("Grace", "Hopper", 135_000, 52, "Engineering Director", "2015-06-15T00:00:00Z", null));
        employeeService.createEmployee(mockRequest(
                "Alan", "Turing", 98_000, 41, "Security Analyst", "2017-09-04T00:00:00Z", "2023-12-31T00:00:00Z"));
    }

    private static CreateEmployeeRequest mockRequest(
            String firstName,
            String lastName,
            int salary,
            int age,
            String jobTitle,
            String hireDate,
            String terminationDate) {
        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setSalary(salary);
        request.setAge(age);
        request.setJobTitle(jobTitle);
        request.setEmail((firstName + "." + lastName + "@example.com").toLowerCase());
        request.setContractHireDate(Instant.parse(hireDate));
        request.setContractTerminationDate(terminationDate == null ? null : Instant.parse(terminationDate));
        return request;
    }
}
