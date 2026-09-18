package com.challenge.api.controller;

import com.challenge.api.dto.CreateEmployeeRequest;
import com.challenge.api.dto.TerminationDateRequest;
import com.challenge.api.model.Employee;
import com.challenge.api.service.EmployeeService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Translates between HTTP and the Service layer. Holds no business rules and no state.
 */
@RestController
@RequestMapping("/api/v1/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    /**
     * @return All Employees, unfiltered.
     */
    @GetMapping
    public List<Employee> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    /**
     * @param uuid Employee UUID; a value that is not a valid UUID is rejected with 400 before this method is reached
     * @return Requested Employee if exists, otherwise 404
     */
    @GetMapping("/{uuid}")
    public Employee getEmployeeByUuid(@PathVariable UUID uuid) {
        return employeeService.getEmployeeByUuid(uuid);
    }

    /**
     * @param requestBody attributes of the Employee to create; rejected with 400 if it fails validation
     * @return Newly created Employee
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Employee createEmployee(@Valid @RequestBody CreateEmployeeRequest requestBody) {
        return employeeService.createEmployee(requestBody);
    }

    /**
     * Sets the date an Employee's contract ends; the Employee itself is kept. PUT because it is idempotent: a web hook that retries the same
     * request, or corrects the date, leaves one consistent result.
     *
     * @param uuid Employee UUID
     * @param requestBody the termination date; rejected with 400 if absent or before the Employee's hire date
     * @return Updated Employee if exists, otherwise 404
     */
    @PutMapping("/{uuid}/termination-date")
    public Employee setTerminationDate(
            @PathVariable UUID uuid, @Valid @RequestBody TerminationDateRequest requestBody) {
        return employeeService.setTerminationDate(uuid, requestBody);
    }
}
