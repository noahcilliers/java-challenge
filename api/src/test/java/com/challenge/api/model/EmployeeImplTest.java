package com.challenge.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class EmployeeImplTest {

    @Test
    void fullNameIsDerivedFromFirstAndLastName() {
        Employee employee = new EmployeeImpl();
        employee.setFirstName("Jane");
        employee.setLastName("Smith");

        assertThat(employee.getFullName()).isEqualTo("Jane Smith");
    }

    @Test
    void fullNameFollowsAChangeOfLastName() {
        Employee employee = new EmployeeImpl();
        employee.setFirstName("Jane");
        employee.setLastName("Smith");

        employee.setLastName("Doe");

        assertThat(employee.getFullName()).isEqualTo("Jane Doe");
    }

    @Test
    void setFullNameDoesNotOverrideTheDerivedName() {
        Employee employee = new EmployeeImpl();
        employee.setFirstName("Jane");
        employee.setLastName("Smith");

        employee.setFullName("Somebody Else");

        assertThat(employee.getFullName()).isEqualTo("Jane Smith");
    }

    @Test
    void toStringExposesOnlyTheUuid() {
        UUID uuid = UUID.randomUUID();
        Employee employee = new EmployeeImpl();
        employee.setUuid(uuid);
        employee.setFirstName("Jane");
        employee.setLastName("Smith");
        employee.setEmail("jane.smith@example.com");
        employee.setSalary(98765);

        assertThat(employee.toString())
                .contains(uuid.toString())
                .doesNotContain("Jane", "Smith", "jane.smith@example.com", "98765");
    }
}
