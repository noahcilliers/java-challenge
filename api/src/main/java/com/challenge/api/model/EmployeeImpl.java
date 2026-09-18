package com.challenge.api.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Plain in-memory representation of an {@link Employee}. Holds state only; rules about how that state is populated
 * live in the Service layer.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class EmployeeImpl implements Employee {

    /** The only attribute safe to appear in logs; everything else is personal data. */
    @ToString.Include
    private UUID uuid;

    private String firstName;
    private String lastName;
    private Integer salary;
    private Integer age;
    private String jobTitle;
    private String email;
    private Instant contractHireDate;
    private Instant contractTerminationDate;

    /** Derived rather than stored, so it can never disagree with the first and last name. */
    @Override
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /** Intentionally a no-op: the full name is derived, and splitting one back into first and last is a guess. */
    @Override
    public void setFullName(String name) {}
}
