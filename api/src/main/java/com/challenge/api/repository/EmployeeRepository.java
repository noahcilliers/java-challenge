package com.challenge.api.repository;

import com.challenge.api.model.Employee;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

/**
 * In-memory stand-in for a persistence layer. Contents are lost on restart. Requests are served on many threads at
 * once, hence the concurrent map.
 */
@Repository
public class EmployeeRepository {

    private final Map<UUID, Employee> employees = new ConcurrentHashMap<>();

    public Employee save(Employee employee) {
        employees.put(employee.getUuid(), employee);
        return employee;
    }

    public Optional<Employee> findByUuid(UUID uuid) {
        return Optional.ofNullable(employees.get(uuid));
    }

    /**
     * @return a snapshot, so callers cannot modify the store through it. Order is not guaranteed.
     */
    public List<Employee> findAll() {
        return List.copyOf(employees.values());
    }
}
