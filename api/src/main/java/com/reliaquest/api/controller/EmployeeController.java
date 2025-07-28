package com.reliaquest.api.controller;

import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import com.reliaquest.api.service.EmployeeService;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/employee")
@RequiredArgsConstructor
@Slf4j
public class EmployeeController implements IEmployeeController<Employee, EmployeeInput> {

    private final EmployeeService employeeService;

    @Override
    public ResponseEntity<List<Employee>> getAllEmployees() {
        log.info("Request to get all employees");
        try {
            List<Employee> employees = employeeService.getAllEmployees();
            return ResponseEntity.ok(employees);
        } catch (Exception e) {
            log.error("Error getting all employees: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(String searchString) {
        log.info("Request to search employees by name: {}", searchString);
        try {
            List<Employee> allEmployees = employeeService.getAllEmployees();
            List<Employee> filteredEmployees = allEmployees.stream()
                    .filter(employee -> employee.getEmployeeName() != null
                            && employee.getEmployeeName().toLowerCase().contains(searchString.toLowerCase()))
                    .collect(Collectors.toList());

            log.info("Found {} employees matching search term: {}", filteredEmployees.size(), searchString);
            return ResponseEntity.ok(filteredEmployees);
        } catch (Exception e) {
            log.error("Error searching employees by name {}: {}", searchString, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<Employee> getEmployeeById(String id) {
        log.info("Request to get employee by id: {}", id);
        try {
            Employee employee = employeeService.getEmployeeById(id);
            if (employee != null) {
                return ResponseEntity.ok(employee);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error getting employee by id {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        log.info("Request to get highest salary of employees");
        try {
            List<Employee> employees = employeeService.getAllEmployees();
            Integer highestSalary = employees.stream()
                    .filter(employee -> employee.getEmployeeSalary() != null)
                    .mapToInt(Employee::getEmployeeSalary)
                    .max()
                    .orElse(0);

            log.info("Highest salary found: {}", highestSalary);
            return ResponseEntity.ok(highestSalary);
        } catch (Exception e) {
            log.error("Error getting highest salary: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        log.info("Request to get top 10 highest earning employee names");
        try {
            List<Employee> employees = employeeService.getAllEmployees();
            List<String> topTenNames = employees.stream()
                    .filter(employee -> employee.getEmployeeSalary() != null)
                    .sorted(Comparator.comparing(Employee::getEmployeeSalary).reversed())
                    .limit(10)
                    .map(Employee::getEmployeeName)
                    .collect(Collectors.toList());

            log.info("Found top {} highest earning employees", topTenNames.size());
            return ResponseEntity.ok(topTenNames);
        } catch (Exception e) {
            log.error("Error getting top 10 highest earning employees: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<Employee> createEmployee(EmployeeInput employeeInput) {
        log.info("Request to create employee: {}", employeeInput.getName());
        try {
            // Validate input
            if (employeeInput.getName() == null
                    || employeeInput.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (employeeInput.getSalary() == null || employeeInput.getSalary() <= 0) {
                return ResponseEntity.badRequest().build();
            }
            if (employeeInput.getAge() == null || employeeInput.getAge() < 16 || employeeInput.getAge() > 75) {
                return ResponseEntity.badRequest().build();
            }
            if (employeeInput.getTitle() == null
                    || employeeInput.getTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            Employee createdEmployee = employeeService.createEmployee(employeeInput);
            if (createdEmployee != null) {
                return ResponseEntity.status(HttpStatus.CREATED).body(createdEmployee);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } catch (Exception e) {
            log.error("Error creating employee {}: {}", employeeInput.getName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<String> deleteEmployeeById(String id) {
        log.info("Request to delete employee by id: {}", id);
        try {
            // First get the employee to find their name
            Employee employee = employeeService.getEmployeeById(id);
            if (employee == null) {
                return ResponseEntity.notFound().build();
            }

            String employeeName = employee.getEmployeeName();
            boolean deleted = employeeService.deleteEmployeeByName(employeeName);

            if (deleted) {
                log.info("Successfully deleted employee: {}", employeeName);
                return ResponseEntity.ok(employeeName);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } catch (Exception e) {
            log.error("Error deleting employee by id {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
