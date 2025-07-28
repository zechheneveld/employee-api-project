package com.reliaquest.api.service;

import com.reliaquest.api.model.ApiResponse;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Employee Service Unit Tests")
class EmployeeServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private EmployeeService employeeService;
    private final String baseUrl = "http://localhost:8112/api/v1/employee";

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService(restTemplate, baseUrl);
    }

    @Test
    @DisplayName("Should get all employees successfully")
    void getAllEmployees_Success() {
        // Given
        Employee employee1 = new Employee("1", "John Doe", 50000, 30, "Developer", "john@company.com");
        Employee employee2 = new Employee("2", "Jane Smith", 60000, 25, "Designer", "jane@company.com");
        List<Employee> employees = Arrays.asList(employee1, employee2);

        ApiResponse<List<Employee>> apiResponse = new ApiResponse<>(employees, "success");
        ResponseEntity<ApiResponse<List<Employee>>> responseEntity =
            new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.exchange(
            eq(baseUrl),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        // When
        List<Employee> result = employeeService.getAllEmployees();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).getEmployeeName());
        assertEquals("Jane Smith", result.get(1).getEmployeeName());

        verify(restTemplate).exchange(
            eq(baseUrl),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class)
        );
    }

    @Test
    @DisplayName("Should return empty list when API returns null data")
    void getAllEmployees_NullData() {
        // Given
        ApiResponse<List<Employee>> apiResponse = new ApiResponse<>(null, "success");
        ResponseEntity<ApiResponse<List<Employee>>> responseEntity =
            new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.exchange(
            eq(baseUrl),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        // When
        List<Employee> result = employeeService.getAllEmployees();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should throw exception when RestTemplate fails")
    void getAllEmployees_RestTemplateException() {
        // Given
        when(restTemplate.exchange(
            eq(baseUrl),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class)
        )).thenThrow(new RestClientException("Connection failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> employeeService.getAllEmployees());

        assertEquals("Failed to fetch employees", exception.getMessage());
        assertTrue(exception.getCause() instanceof RestClientException);
    }

    @Test
    @DisplayName("Should get employee by ID successfully")
    void getEmployeeById_Success() {
        // Given
        String employeeId = "123";
        Employee employee = new Employee(employeeId, "John Doe", 50000, 30, "Developer", "john@company.com");
        ApiResponse<Employee> apiResponse = new ApiResponse<>(employee, "success");
        ResponseEntity<ApiResponse<Employee>> responseEntity =
            new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.exchange(
            eq(baseUrl + "/" + employeeId),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        // When
        Employee result = employeeService.getEmployeeById(employeeId);

        // Then
        assertNotNull(result);
        assertEquals(employeeId, result.getId());
        assertEquals("John Doe", result.getEmployeeName());
        assertEquals(50000, result.getEmployeeSalary());
    }

    @Test
    @DisplayName("Should return null when employee not found")
    void getEmployeeById_NotFound() {
        // Given
        String employeeId = "999";
        ApiResponse<Employee> apiResponse = new ApiResponse<>(null, "not found");
        ResponseEntity<ApiResponse<Employee>> responseEntity =
            new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);

        when(restTemplate.exchange(
            eq(baseUrl + "/" + employeeId),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        // When
        Employee result = employeeService.getEmployeeById(employeeId);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should create employee successfully")
    void createEmployee_Success() {
        // Given
        EmployeeInput input = new EmployeeInput("New Employee", 55000, 28, "Analyst");
        Employee createdEmployee = new Employee("456", "New Employee", 55000, 28, "Analyst", "new@company.com");
        ApiResponse<Employee> apiResponse = new ApiResponse<>(createdEmployee, "success");
        ResponseEntity<ApiResponse<Employee>> responseEntity =
            new ResponseEntity<>(apiResponse, HttpStatus.CREATED);

        when(restTemplate.exchange(
            eq(baseUrl),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        // When
        Employee result = employeeService.createEmployee(input);

        // Then
        assertNotNull(result);
        assertEquals("456", result.getId());
        assertEquals("New Employee", result.getEmployeeName());
        assertEquals(55000, result.getEmployeeSalary());

        // Verify the request body was constructed correctly
        ArgumentCaptor<HttpEntity> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
            eq(baseUrl),
            eq(HttpMethod.POST),
            requestCaptor.capture(),
            any(ParameterizedTypeReference.class)
        );

        HttpEntity<?> capturedRequest = requestCaptor.getValue();
        assertNotNull(capturedRequest.getBody());
        assertTrue(capturedRequest.getBody() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, Object> requestBody = (Map<String, Object>) capturedRequest.getBody();
        assertEquals("New Employee", requestBody.get("name"));
        assertEquals(55000, requestBody.get("salary"));
        assertEquals(28, requestBody.get("age"));
        assertEquals("Analyst", requestBody.get("title"));
    }

    @Test
    @DisplayName("Should return null when create employee fails")
    void createEmployee_Failure() {
        // Given
        EmployeeInput input = new EmployeeInput("New Employee", 55000, 28, "Analyst");
        ApiResponse<Employee> apiResponse = new ApiResponse<>(null, "error");
        ResponseEntity<ApiResponse<Employee>> responseEntity =
            new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);

        when(restTemplate.exchange(
            eq(baseUrl),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        // When
        Employee result = employeeService.createEmployee(input);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should delete employee successfully")
    void deleteEmployeeByName_Success() {
        // Given
        String employeeName = "John Doe";
        ApiResponse<Boolean> apiResponse = new ApiResponse<>(true, "success");
        ResponseEntity<ApiResponse<Boolean>> responseEntity =
            new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.exchange(
            eq(baseUrl),
            eq(HttpMethod.DELETE),
            any(HttpEntity.class),
            any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        // When
        boolean result = employeeService.deleteEmployeeByName(employeeName);

        // Then
        assertTrue(result);

        // Verify the request body was constructed correctly
        ArgumentCaptor<HttpEntity> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
            eq(baseUrl),
            eq(HttpMethod.DELETE),
            requestCaptor.capture(),
            any(ParameterizedTypeReference.class)
        );

        HttpEntity<?> capturedRequest = requestCaptor.getValue();
        assertNotNull(capturedRequest.getBody());
        assertTrue(capturedRequest.getBody() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, Object> requestBody = (Map<String, Object>) capturedRequest.getBody();
        assertEquals(employeeName, requestBody.get("name"));
    }

    @Test
    @DisplayName("Should return false when delete employee fails")
    void deleteEmployeeByName_Failure() {
        // Given
        String employeeName = "John Doe";
        ApiResponse<Boolean> apiResponse = new ApiResponse<>(false, "error");
        ResponseEntity<ApiResponse<Boolean>> responseEntity =
            new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);

        when(restTemplate.exchange(
            eq(baseUrl),
            eq(HttpMethod.DELETE),
            any(HttpEntity.class),
            any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        // When
        boolean result = employeeService.deleteEmployeeByName(employeeName);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should throw exception when delete operation fails with RestTemplate exception")
    void deleteEmployeeByName_RestTemplateException() {
        // Given
        String employeeName = "John Doe";
        when(restTemplate.exchange(
            eq(baseUrl),
            eq(HttpMethod.DELETE),
            any(HttpEntity.class),
            any(ParameterizedTypeReference.class)
        )).thenThrow(new RestClientException("Network error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> employeeService.deleteEmployeeByName(employeeName));

        assertEquals("Failed to delete employee", exception.getMessage());
        assertTrue(exception.getCause() instanceof RestClientException);
    }
}
