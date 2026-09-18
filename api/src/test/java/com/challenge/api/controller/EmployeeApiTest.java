package com.challenge.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Exercises the whole application over HTTP: routing, JSON, validation, the Service, the store and error handling. The
 * store starts with the seeded mock Employees and is shared between tests, so no test assumes an exact total.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class EmployeeApiTest {

    private static final String EMPLOYEES = "/api/v1/employee";
    private static final String VALID_EMPLOYEE =
            """
            {
              "firstName": "Jane",
              "lastName": "Smith",
              "salary": 85000,
              "age": 34,
              "jobTitle": "Software Engineer",
              "email": "jane.smith@example.com",
              "contractHireDate": "2024-01-15T00:00:00Z"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAllReturnsTheSeededEmployeesWithEveryContractAttribute() throws Exception {
        mockMvc.perform(get(EMPLOYEES))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$[0].uuid").isNotEmpty())
                .andExpect(jsonPath("$[0].firstName").isNotEmpty())
                .andExpect(jsonPath("$[0].lastName").isNotEmpty())
                .andExpect(jsonPath("$[0].fullName").isNotEmpty())
                .andExpect(jsonPath("$[0].salary").isNumber())
                .andExpect(jsonPath("$[0].age").isNumber())
                .andExpect(jsonPath("$[0].jobTitle").isNotEmpty())
                .andExpect(jsonPath("$[0].email").isNotEmpty())
                .andExpect(jsonPath("$[0].contractHireDate").isNotEmpty())
                .andExpect(jsonPath("$[0]", hasKey("contractTerminationDate")));
    }

    @Test
    void createReturns201WithTheCreatedEmployee() throws Exception {
        create(VALID_EMPLOYEE)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").isNotEmpty())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.fullName").value("Jane Smith"))
                .andExpect(jsonPath("$.salary").value(85000))
                .andExpect(jsonPath("$.age").value(34))
                .andExpect(jsonPath("$.jobTitle").value("Software Engineer"))
                .andExpect(jsonPath("$.email").value("jane.smith@example.com"))
                .andExpect(jsonPath("$.contractHireDate").value("2024-01-15T00:00:00Z"))
                .andExpect(jsonPath("$.contractTerminationDate").isEmpty());
    }

    @Test
    void aCreatedEmployeeCanBeFetchedByUuidAndAppearsInGetAll() throws Exception {
        String uuid = uuidOf(create(VALID_EMPLOYEE));

        mockMvc.perform(get(EMPLOYEES + "/" + uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(uuid))
                .andExpect(jsonPath("$.fullName").value("Jane Smith"));
        mockMvc.perform(get(EMPLOYEES)).andExpect(jsonPath("$[*].uuid", hasItem(uuid)));
    }

    @Test
    void createIgnoresAUuidChosenByTheCaller() throws Exception {
        String chosenUuid = "11111111-1111-1111-1111-111111111111";
        String body = VALID_EMPLOYEE.replaceFirst("\\{", "{ \"uuid\": \"" + chosenUuid + "\",");

        create(body).andExpect(status().isCreated()).andExpect(jsonPath("$.uuid", not(chosenUuid)));
        mockMvc.perform(get(EMPLOYEES + "/" + chosenUuid)).andExpect(status().isNotFound());
    }

    @Test
    void createAcceptsAnOptionalTerminationDate() throws Exception {
        String body = VALID_EMPLOYEE.replaceFirst("\\{", "{ \"contractTerminationDate\": \"2025-06-30T00:00:00Z\",");

        create(body)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contractTerminationDate").value("2025-06-30T00:00:00Z"));
    }

    @Test
    void createWithNoAttributesReturns400NamingEveryRequiredAttribute() throws Exception {
        create("{}")
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath(
                        "$.errors.keys()",
                        containsInAnyOrder(
                                "firstName", "lastName", "salary", "age", "jobTitle", "email", "contractHireDate")));
    }

    @Test
    void createWithInvalidValuesReturns400WithoutRepeatingThem() throws Exception {
        String body =
                VALID_EMPLOYEE.replace("jane.smith@example.com", "not-an-email").replace("85000", "-77");

        create(body)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.keys()", containsInAnyOrder("email", "salary")))
                .andExpect(content().string(not(containsString("not-an-email"))))
                .andExpect(content().string(not(containsString("-77"))));
    }

    @Test
    void createWithTerminationBeforeHireReturns400() throws Exception {
        String body = VALID_EMPLOYEE.replaceFirst("\\{", "{ \"contractTerminationDate\": \"2020-01-01T00:00:00Z\",");

        create(body)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.contractTerminationDateValid")
                        .value("contractTerminationDate must not be before contractHireDate"));
    }

    @Test
    void createWithMalformedJsonReturns400() throws Exception {
        create("{ this is not json").andExpect(status().isBadRequest());
    }

    @Test
    void createWithAnUnparseableDateReturns400() throws Exception {
        create(VALID_EMPLOYEE.replace("2024-01-15T00:00:00Z", "15/01/2024")).andExpect(status().isBadRequest());
    }

    @Test
    void createWithAWrongTypeReturns400() throws Exception {
        create(VALID_EMPLOYEE.replace("85000", "\"a lot\"")).andExpect(status().isBadRequest());
    }

    @Test
    void createWithoutAJsonContentTypeReturns415() throws Exception {
        mockMvc.perform(post(EMPLOYEES).contentType(MediaType.TEXT_PLAIN).content(VALID_EMPLOYEE))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void getByUnknownUuidReturns404() throws Exception {
        mockMvc.perform(get(EMPLOYEES + "/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail", containsString("00000000-0000-0000-0000-000000000000")));
    }

    @Test
    void getByMalformedUuidReturns400() throws Exception {
        mockMvc.perform(get(EMPLOYEES + "/not-a-uuid")).andExpect(status().isBadRequest());
    }

    @Test
    void unsupportedVerbsReturn405RatherThan500() throws Exception {
        mockMvc.perform(put(EMPLOYEES).contentType(MediaType.APPLICATION_JSON).content(VALID_EMPLOYEE))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(delete(EMPLOYEES + "/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void logsIdentifyTheEmployeeByUuidAndNeverContainPersonalData(CapturedOutput output) throws Exception {
        String body = VALID_EMPLOYEE
                .replace("Jane", "Zebediah")
                .replace("jane.smith@example.com", "zebediah@example.com")
                .replace("85000", "31337");
        String uuid = uuidOf(create(body));
        create(body.replace("zebediah@example.com", "zebediah-at-example"));

        assertThat(output.getOut())
                .contains("Created employee uuid=" + uuid)
                .contains("Validation failed on attributes [email]")
                .doesNotContain("Zebediah", "zebediah@example.com", "zebediah-at-example", "31337");
    }

    private ResultActions create(String body) throws Exception {
        return mockMvc.perform(
                post(EMPLOYEES).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private static String uuidOf(ResultActions created) throws Exception {
        return JsonPath.read(created.andReturn().getResponse().getContentAsString(), "$.uuid");
    }
}
