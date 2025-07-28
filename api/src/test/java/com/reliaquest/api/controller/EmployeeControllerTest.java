package com.reliaquest.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import com.reliaquest.api.service.EmployeeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
@DisplayName("Employee Controller Unit Tests")
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/v1/employee - Should return all employees")
    void getAllEmployees_Success() throws Exception {
        // Given
        Employee employee1 = new Employee("1", "John Doe", 50000, 30, "Developer", "john@company.com");
        Employee employee2 = new Employee("2", "Jane Smith", 60000, 25, "Designer", "jane@company.com");
        List<Employee> employees = Arrays.asList(employee1, employee2);

        when(employeeService.getAllEmployees()).thenReturn(employees);

        // When & Then
        mockMvc.perform(get("/api/v1/employee"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("1")))
                .andExpect(jsonPath("$[0].employee_name", is("John Doe")))
                .andExpect(jsonPath("$[0].employee_salary", is(50000)))
                .andExpect(jsonPath("$[1].id", is("2")))
                .andExpect(jsonPath("$[1].employee_name", is("Jane Smith")))
                .andExpect(jsonPath("$[1].employee_salary", is(60000)));

        verify(employeeService).getAllEmployees();
    }

    @Test
    @DisplayName("GET /api/v1/employee - Should return empty list when no employees")
    void getAllEmployees_EmptyList() throws Exception {
        // Given
        when(employeeService.getAllEmployees()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/v1/employee"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(employeeService).getAllEmployees();
    }

    @Test
    @DisplayName("GET /api/v1/employee - Should return 500 when service throws exception")
    void getAllEmployees_ServiceException() throws Exception {
        // Given
        when(employeeService.getAllEmployees()).thenThrow(new RuntimeException("Service error"));

        // When & Then
        mockMvc.perform(get("/api/v1/employee"))
                .andExpect(status().isInternalServerError());

        verify(employeeService).getAllEmployees();
    }

    @Test
    @DisplayName("GET /api/v1/employee/{id} - Should return employee by ID")
    void getEmployeeById_Success() throws Exception {
        // Given
        String employeeId = "123";
        Employee employee = new Employee(employeeId, "John Doe", 50000, 30, "Developer", "john@company.com");

        when(employeeService.getEmployeeById(employeeId)).thenReturn(employee);

        // When & Then
        mockMvc.perform(get("/api/v1/employee/{id}", employeeId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(employeeId)))
                .andExpect(jsonPath("$.employee_name", is("John Doe")))
                .andExpect(jsonPath("$.employee_salary", is(50000)));

        verify(employeeService).getEmployeeById(employeeId);
    }

    @Test
    @DisplayName("GET /api/v1/employee/{id} - Should return 404 when employee not found")
    void getEmployeeById_NotFound() throws Exception {
        // Given
        String employeeId = "999";
        when(employeeService.getEmployeeById(employeeId)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/v1/employee/{id}", employeeId))
                .andExpect(status().isNotFound());

        verify(employeeService).getEmployeeById(employeeId);
    }

    @Test
    @DisplayName("GET /api/v1/employee/search/{searchString} - Should return filtered employees")
    void getEmployeesByNameSearch_Success() throws Exception {
        // Given
        String searchString = "john";
        Employee employee1 = new Employee("1", "John Doe", 50000, 30, "Developer", "john@company.com");
        Employee employee2 = new Employee("2", "Johnny Smith", 55000, 28, "Designer", "johnny@company.com");
        Employee employee3 = new Employee("3", "Jane Doe", 60000, 25, "Manager", "jane@company.com");
        List<Employee> allEmployees = Arrays.asList(employee1, employee2, employee3);

        when(employeeService.getAllEmployees()).thenReturn(allEmployees);

        // When & Then
        mockMvc.perform(get("/api/v1/employee/search/{searchString}", searchString))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].employee_name", containsStringIgnoringCase("john")))
                .andExpect(jsonPath("$[1].employee_name", containsStringIgnoringCase("john")));

        verify(employeeService).getAllEmployees();
    }

    @Test
    @DisplayName("GET /api/v1/employee/highestSalary - Should return highest salary")
    void getHighestSalaryOfEmployees_Success() throws Exception {
        // Given
        Employee employee1 = new Employee("1", "John Doe", 50000, 30, "Developer", "john@company.com");
        Employee employee2 = new Employee("2", "Jane Smith", 80000, 25, "Senior Developer", "jane@company.com");
        Employee employee3 = new Employee("3", "Bob Johnson", 60000, 35, "Manager", "bob@company.com");
        List<Employee> employees = Arrays.asList(employee1, employee2, employee3);

        when(employeeService.getAllEmployees()).thenReturn(employees);

        // When & Then
        mockMvc.perform(get("/api/v1/employee/highestSalary"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("80000"));

        verify(employeeService).getAllEmployees();
    }

    @Test
    @DisplayName("GET /api/v1/employee/topTenHighestEarningEmployeeNames - Should return top earners")
    void getTopTenHighestEarningEmployeeNames_Success() throws Exception {
        // Given
        Employee employee1 = new Employee("1", "John Doe", 50000, 30, "Developer", "john@company.com");
        Employee employee2 = new Employee("2", "Jane Smith", 80000, 25, "Senior Developer", "jane@company.com");
        Employee employee3 = new Employee("3", "Bob Johnson", 60000, 35, "Manager", "bob@company.com");
        List<Employee> employees = Arrays.asList(employee1, employee2, employee3);

        when(employeeService.getAllEmployees()).thenReturn(employees);

        // When & Then
        mockMvc.perform(get("/api/v1/employee/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0]", is("Jane Smith")))  // Highest salary first
                .andExpect(jsonPath("$[1]", is("Bob Johnson")))
                .andExpect(jsonPath("$[2]", is("John Doe")));

        verify(employeeService).getAllEmployees();
    }

    @Test
    @DisplayName("POST /api/v1/employee - Should create employee successfully")
    void createEmployee_Success() throws Exception {
        // Given
        EmployeeInput input = new EmployeeInput("New Employee", 55000, 28, "Analyst");
        Employee createdEmployee = new Employee("456", "New Employee", 55000, 28, "Analyst", "new@company.com");

        when(employeeService.createEmployee(any(EmployeeInput.class))).thenReturn(createdEmployee);

        // When & Then
        mockMvc.perform(post("/api/v1/employee")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is("456")))
                .andExpect(jsonPath("$.employee_name", is("New Employee")))
                .andExpect(jsonPath("$.employee_salary", is(55000)));

        verify(employeeService).createEmployee(any(EmployeeInput.class));
    }

    @Test
    @DisplayName("POST /api/v1/employee - Should return 400 for invalid name")
    void createEmployee_InvalidName() throws Exception {
        // Given
        EmployeeInput input = new EmployeeInput("", 55000, 28, "Analyst");

        // When & Then
        mockMvc.perform(post("/api/v1/employee")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).createEmployee(any(EmployeeInput.class));
    }

    @Test
    @DisplayName("POST /api/v1/employee - Should return 400 for invalid salary")
    void createEmployee_InvalidSalary() throws Exception {
        // Given
        EmployeeInput input = new EmployeeInput("John Doe", -1000, 28, "Analyst");

        // When & Then
        mockMvc.perform(post("/api/v1/employee")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).createEmployee(any(EmployeeInput.class));
    }

    @Test
    @DisplayName("POST /api/v1/employee - Should return 400 for invalid age")
    void createEmployee_InvalidAge() throws Exception {
        // Given
        EmployeeInput input = new EmployeeInput("John Doe", 55000, 15, "Analyst"); // Age below 16

        // When & Then
        mockMvc.perform(post("/api/v1/employee")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).createEmployee(any(EmployeeInput.class));
    }

    @Test
    @DisplayName("POST /api/v1/employee - Should return 500 when service returns null")
    void createEmployee_ServiceReturnsNull() throws Exception {
        // Given
        EmployeeInput input = new EmployeeInput("John Doe", 55000, 28, "Analyst");
        when(employeeService.createEmployee(any(EmployeeInput.class))).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/api/v1/employee")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isInternalServerError());

        verify(employeeService).createEmployee(any(EmployeeInput.class));
    }

    @Test
    @DisplayName("DELETE /api/v1/employee/{id} - Should delete employee successfully")
    void deleteEmployeeById_Success() throws Exception {
        // Given
        String employeeId = "123";
        Employee employee = new Employee(employeeId, "John Doe", 50000, 30, "Developer", "john@company.com");

        when(employeeService.getEmployeeById(employeeId)).thenReturn(employee);
        when(employeeService.deleteEmployeeByName("John Doe")).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/v1/employee/{id}", employeeId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"))
                .andExpect(content().string("John Doe"));

        verify(employeeService).getEmployeeById(employeeId);
        verify(employeeService).deleteEmployeeByName("John Doe");
    }

    @Test
    @DisplayName("DELETE /api/v1/employee/{id} - Should return 404 when employee not found")
    void deleteEmployeeById_NotFound() throws Exception {
        // Given
        String employeeId = "999";
        when(employeeService.getEmployeeById(employeeId)).thenReturn(null);

        // When & Then
        mockMvc.perform(delete("/api/v1/employee/{id}", employeeId))
                .andExpect(status().isNotFound());

        verify(employeeService).getEmployeeById(employeeId);
        verify(employeeService, never()).deleteEmployeeByName(anyString());
    }

    @Test
    @DisplayName("DELETE /api/v1/employee/{id} - Should return 500 when delete fails")
    void deleteEmployeeById_DeleteFails() throws Exception {
        // Given
        String employeeId = "123";
        Employee employee = new Employee(employeeId, "John Doe", 50000, 30, "Developer", "john@company.com");

        when(employeeService.getEmployeeById(employeeId)).thenReturn(employee);
        when(employeeService.deleteEmployeeByName("John Doe")).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/api/v1/employee/{id}", employeeId))
                .andExpect(status().isInternalServerError());

        verify(employeeService).getEmployeeById(employeeId);
        verify(employeeService).deleteEmployeeByName("John Doe");
    }
}
