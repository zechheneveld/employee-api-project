package com.reliaquest.api.service;

import com.reliaquest.api.model.ApiResponse;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class EmployeeService {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public EmployeeService(
            RestTemplate restTemplate,
            @Value("${employee.api.base-url:http://localhost:8112/api/v1/employee}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public List<Employee> getAllEmployees() {
        log.info("Fetching all employees from mock API");
        try {
            ResponseEntity<ApiResponse<List<Employee>>> response = restTemplate.exchange(
                    baseUrl, HttpMethod.GET, null, new ParameterizedTypeReference<ApiResponse<List<Employee>>>() {});

            if (response.getBody() != null && response.getBody().getData() != null) {
                log.info(
                        "Successfully fetched {} employees",
                        response.getBody().getData().size());
                return response.getBody().getData();
            }
            log.warn("Received null or empty response from mock API");
            return List.of();
        } catch (Exception e) {
            log.error("Error fetching all employees: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch employees", e);
        }
    }

    public Employee getEmployeeById(String id) {
        log.info("Fetching employee with id: {}", id);
        try {
            ResponseEntity<ApiResponse<Employee>> response = restTemplate.exchange(
                    baseUrl + "/" + id,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<Employee>>() {});

            if (response.getBody() != null && response.getBody().getData() != null) {
                log.info(
                        "Successfully fetched employee: {}",
                        response.getBody().getData().getEmployee_name());
                return response.getBody().getData();
            }
            log.warn("Employee with id {} not found", id);
            return null;
        } catch (Exception e) {
            log.error("Error fetching employee with id {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch employee", e);
        }
    }

    public Employee createEmployee(EmployeeInput employeeInput) {
        log.info("Creating new employee: {}", employeeInput.getName());
        try {
            // Create a properly structured request with correct headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Map EmployeeInput to the format expected by mock server
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("name", employeeInput.getName());
            requestBody.put("salary", employeeInput.getSalary());
            requestBody.put("age", employeeInput.getAge());
            requestBody.put("title", employeeInput.getTitle());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<ApiResponse<Employee>> response = restTemplate.exchange(
                    baseUrl, HttpMethod.POST, request, new ParameterizedTypeReference<ApiResponse<Employee>>() {});

            if (response.getBody() != null && response.getBody().getData() != null) {
                Employee createdEmployee = response.getBody().getData();
                log.info("Successfully created employee with id: {}", createdEmployee.getId());
                return createdEmployee;
            }
            log.warn("Failed to create employee - received null response");
            return null;
        } catch (Exception e) {
            log.error("Error creating employee {}: {}", employeeInput.getName(), e.getMessage(), e);
            throw new RuntimeException("Failed to create employee", e);
        }
    }

    public boolean deleteEmployeeByName(String name) {
        log.info("Deleting employee with name: {}", name);
        try {
            // Create proper request body and headers for delete operation
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create request body with employee name as expected by mock server
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("name", name);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<ApiResponse<Boolean>> response = restTemplate.exchange(
                    baseUrl, HttpMethod.DELETE, request, new ParameterizedTypeReference<ApiResponse<Boolean>>() {});

            if (response.getBody() != null && response.getBody().getData() != null) {
                boolean deleted = response.getBody().getData();
                log.info("Employee deletion result for {}: {}", name, deleted);
                return deleted;
            }
            log.warn("Failed to delete employee - received null response");
            return false;
        } catch (Exception e) {
            log.error("Error deleting employee {}: {}", name, e.getMessage(), e);
            throw new RuntimeException("Failed to delete employee", e);
        }
    }
}
