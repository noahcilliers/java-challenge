package com.challenge.api.exception;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.challenge.api.controller.EmployeeController;
import com.challenge.api.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

/** A failure nobody planned for can only be provoked with a mocked Service, hence a separate, web-layer-only test. */
@WebMvcTest(EmployeeController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Test
    void anUnexpectedFailureReturns500WithoutLeakingInternals() throws Exception {
        when(employeeService.getAllEmployees()).thenThrow(new IllegalStateException("jdbc://internal-host:5432"));

        mockMvc.perform(get("/api/v1/employee"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred"))
                .andExpect(content().string(not(containsString("internal-host"))))
                .andExpect(content().string(not(containsString("IllegalStateException"))));
    }
}
