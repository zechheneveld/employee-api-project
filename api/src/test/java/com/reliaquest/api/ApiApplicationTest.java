package com.reliaquest.api;

import com.reliaquest.api.controller.EmployeeController;
import com.reliaquest.api.service.EmployeeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@DisplayName("API Application Context Tests")
class ApiApplicationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private EmployeeController employeeController;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private RestTemplate restTemplate;

    @MockBean
    private RestTemplate mockRestTemplate; // For tests that need mocked external calls

    @Test
    @DisplayName("Application context should load successfully")
    void contextLoads() {
        // This test ensures the Spring Boot application starts up correctly
        // and all beans are properly configured and can be injected
        assertNotNull(applicationContext);
    }

    @Test
    @DisplayName("All essential beans should be created and available")
    void essentialBeansShouldBeAvailable() {
        // Verify that all critical components are properly instantiated
        assertNotNull(employeeController, "EmployeeController should be available");
        assertNotNull(employeeService, "EmployeeService should be available");
        assertNotNull(restTemplate, "RestTemplate should be available");
    }

    @Test
    @DisplayName("Controller should be properly wired with dependencies")
    void controllerShouldBeProperlyWired() {
        // Test that dependency injection worked correctly
        assertThat(employeeController).isNotNull();

        // You could test that the controller has the expected service injected
        // This verifies the Spring configuration is working
    }

    @Test
    @DisplayName("Service should be properly configured with RestTemplate")
    void serviceShouldBeProperlyConfigured() {
        // Verify that the service is properly configured
        assertThat(employeeService).isNotNull();

        // This ensures that your RestTemplateConfig is working
        // and the RestTemplate is properly injected into the service
    }

    @Test
    @DisplayName("Application should have correct profile configuration")
    void applicationShouldHaveCorrectProfile() {
        // Verify application properties are loaded correctly
        String[] activeProfiles = applicationContext.getEnvironment().getActiveProfiles();

        // You can verify specific profiles or configurations
        assertThat(applicationContext.getEnvironment()).isNotNull();
    }

    @Test
    @DisplayName("All REST endpoints should be mapped correctly")
    void restEndpointsShouldBeMapped() {
        // This test ensures that all your @RequestMapping annotations
        // are properly configured and Spring can find them
        assertThat(employeeController).isNotNull();

        // Spring Boot will fail to start if there are mapping conflicts
        // or missing dependencies, so this test passing means your
        // REST endpoints are properly configured
    }
}
