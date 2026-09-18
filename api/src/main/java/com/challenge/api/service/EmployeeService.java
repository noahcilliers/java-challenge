package com.challenge.api.service;

import com.challenge.api.dto.CreateEmployeeRequest;
import com.challenge.api.exception.EmployeeNotFoundException;
import com.challenge.api.model.Employee;
import com.challenge.api.model.EmployeeImpl;
import com.challenge.api.repository.EmployeeRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public List<Employee> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        log.debug("Returning {} employees", employees.size());
        return employees;
    }

    /**
     * @throws EmployeeNotFoundException if no Employee has the given UUID
     */
    public Employee getEmployeeByUuid(UUID uuid) {
        return employeeRepository.findByUuid(uuid).orElseThrow(() -> new EmployeeNotFoundException(uuid));
    }

    /**
     * @param request assumed valid; validation happens before the Service is reached
     * @return the stored Employee, with a server-generated UUID
     */
    public Employee createEmployee(CreateEmployeeRequest request) {
        Employee employee = new EmployeeImpl();
        employee.setUuid(UUID.randomUUID());
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setSalary(request.getSalary());
        employee.setAge(request.getAge());
        employee.setJobTitle(request.getJobTitle());
        employee.setEmail(request.getEmail());
        employee.setContractHireDate(request.getContractHireDate());
        employee.setContractTerminationDate(request.getContractTerminationDate());
        employeeRepository.save(employee);
        log.info("Created employee uuid={}", employee.getUuid());
        return employee;
    }
}
