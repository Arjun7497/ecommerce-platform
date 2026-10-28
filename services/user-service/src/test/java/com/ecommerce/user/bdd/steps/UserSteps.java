package com.ecommerce.user.bdd.steps;

import com.ecommerce.user.repository.UserRepository;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class UserSteps {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("bddtestdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;

    private ResultActions resultActions;
    private Map<String, String> currentDetails;

    @Before
    public void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Given("the user service is running")
    public void theUserServiceIsRunning() {
        assertThat(userRepository).isNotNull();
    }

    @Given("no user exists with email {string}")
    public void noUserExistsWithEmail(String email) {
        userRepository.findByEmail(email).ifPresent(u -> userRepository.delete(u));
    }

    @Given("a user already exists with email {string}")
    public void aUserAlreadyExistsWithEmail(String email) throws Exception {
        String body = buildRegisterBody(email, "Password123", "Existing", "User");
        mockMvc.perform(post("/api/v1/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    @Given("a user is registered with email {string} and password {string}")
    public void aUserIsRegisteredWithEmailAndPassword(String email, String password) throws Exception {
        String body = buildRegisterBody(email, password, "Test", "User");
        mockMvc.perform(post("/api/v1/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    @When("I register with the following details:")
    public void iRegisterWithTheFollowingDetails(io.cucumber.datatable.DataTable dataTable) throws Exception {
        currentDetails = dataTable.asMap();
        String body = buildRegisterBody(
                currentDetails.get("email"),
                currentDetails.get("password"),
                currentDetails.get("firstName"),
                currentDetails.get("lastName")
        );
        resultActions = mockMvc.perform(post("/api/v1/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    @When("I login with email {string} and password {string}")
    public void iLoginWithEmailAndPassword(String email, String password) throws Exception {
        String body = String.format("""
                {"email":"%s","password":"%s"}""", email, password);
        resultActions = mockMvc.perform(post("/api/v1/users/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int status) throws Exception {
        resultActions.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().is(status));
    }

    @Then("the response should contain email {string}")
    public void theResponseShouldContainEmail(String email) throws Exception {
        resultActions.andExpect(jsonPath("$.email").value(email));
    }

    @Then("the response should contain firstName {string}")
    public void theResponseShouldContainFirstName(String firstName) throws Exception {
        resultActions.andExpect(jsonPath("$.firstName").value(firstName));
    }

    @Then("the user should be saved in the database")
    public void theUserShouldBeSavedInTheDatabase() {
        assertThat(userRepository.existsByEmail(currentDetails.get("email"))).isTrue();
    }

    @Then("the response should contain error about duplicate email")
    public void theResponseShouldContainErrorAboutDuplicateEmail() throws Exception {
        resultActions.andExpect(jsonPath("$.detail").exists());
    }

    @Then("the response should contain validation error for {string}")
    public void theResponseShouldContainValidationErrorFor(String field) throws Exception {
        resultActions.andExpect(jsonPath("$.properties.errors." + field).exists());
    }

    @Then("the response should contain a JWT token")
    public void theResponseShouldContainAJwtToken() throws Exception {
        resultActions.andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Then("the token type should be {string}")
    public void theTokenTypeShouldBe(String tokenType) throws Exception {
        resultActions.andExpect(jsonPath("$.tokenType").value(tokenType));
    }

    private String buildRegisterBody(String email, String password, String firstName, String lastName) {
        return String.format("""
                {"email":"%s","password":"%s","firstName":"%s","lastName":"%s"}""",
                email, password, firstName, lastName);
    }
}
