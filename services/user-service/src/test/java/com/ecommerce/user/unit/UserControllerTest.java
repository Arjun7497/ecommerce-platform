package com.ecommerce.user.unit;

import com.ecommerce.user.controller.UserController;
import com.ecommerce.user.dto.*;
import com.ecommerce.user.exception.UserAlreadyExistsException;
import com.ecommerce.user.model.User;
import com.ecommerce.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TDD slice tests for UserController.
 * Uses @WebMvcTest — loads only web layer, no full Spring context.
 */
@WebMvcTest(UserController.class)
@DisplayName("UserController Unit Tests")
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private UserService userService;

    @Nested
    @DisplayName("POST /api/v1/users/register")
    class RegisterEndpoint {

        @Test
        @WithMockUser
        @DisplayName("should return 201 with user response on valid input")
        void shouldReturn201_onValidRegistration() throws Exception {
            var request = buildRegisterRequest("new@example.com");
            var response = buildUserResponse("new@example.com");
            when(userService.register(any())).thenReturn(response);

            mockMvc.perform(post("/api/v1/users/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.email").value("new@example.com"))
                    .andExpect(jsonPath("$.firstName").value("John"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return 400 when email is invalid")
        void shouldReturn400_whenEmailInvalid() throws Exception {
            var request = buildRegisterRequest("not-an-email");

            mockMvc.perform(post("/api/v1/users/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("Validation failed"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return 409 when email already exists")
        void shouldReturn409_whenEmailTaken() throws Exception {
            var request = buildRegisterRequest("taken@example.com");
            when(userService.register(any())).thenThrow(new UserAlreadyExistsException("taken@example.com"));

            mockMvc.perform(post("/api/v1/users/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/users/login")
    class LoginEndpoint {

        @Test
        @WithMockUser
        @DisplayName("should return 200 with token on valid credentials")
        void shouldReturn200_onValidLogin() throws Exception {
            var request = new LoginRequest();
            request.setEmail("user@example.com");
            request.setPassword("password123");

            var authResponse = AuthResponse.builder()
                    .token("jwt-token")
                    .tokenType("Bearer")
                    .user(buildUserResponse("user@example.com"))
                    .build();
            when(userService.login(any())).thenReturn(authResponse);

            mockMvc.perform(post("/api/v1/users/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("jwt-token"))
                    .andExpect(jsonPath("$.tokenType").value("Bearer"));
        }
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private RegisterRequest buildRegisterRequest(String email) {
        var req = new RegisterRequest();
        req.setEmail(email);
        req.setPassword("password123");
        req.setFirstName("John");
        req.setLastName("Doe");
        return req;
    }

    private UserResponse buildUserResponse(String email) {
        return UserResponse.builder()
                .id(UUID.randomUUID())
                .email(email)
                .firstName("John")
                .lastName("Doe")
                .role(User.Role.CUSTOMER)
                .active(true)
                .build();
    }
}
