package com.ecommerce.user.unit;

import com.ecommerce.user.dto.LoginRequest;
import com.ecommerce.user.dto.RegisterRequest;
import com.ecommerce.user.exception.UserAlreadyExistsException;
import com.ecommerce.user.exception.UserNotFoundException;
import com.ecommerce.user.model.User;
import com.ecommerce.user.repository.UserRepository;
import com.ecommerce.user.service.JwtService;
import com.ecommerce.user.service.UserService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * TDD Unit tests for UserService.
 * Tests are isolated — no Spring context, no database.
 * Pattern: Arrange → Act → Assert
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    @InjectMocks private UserService userService;

    // ─── register() ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("register()")
    class Register {

        @Test
        @DisplayName("should register user when email is not taken")
        void shouldRegisterUser_whenEmailNotTaken() {
            // Arrange
            var request = buildRegisterRequest("john@example.com", "John", "Doe");
            var savedUser = buildUser(request);
            when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            // Act
            var response = userService.register(request);

            // Assert
            assertThat(response.getEmail()).isEqualTo("john@example.com");
            assertThat(response.getFirstName()).isEqualTo("John");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("should throw UserAlreadyExistsException when email is taken")
        void shouldThrow_whenEmailAlreadyExists() {
            // Arrange
            var request = buildRegisterRequest("existing@example.com", "Jane", "Doe");
            when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> userService.register(request))
                    .isInstanceOf(UserAlreadyExistsException.class)
                    .hasMessageContaining("existing@example.com");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should encode password before saving")
        void shouldEncodePassword_beforeSaving() {
            // Arrange
            var request = buildRegisterRequest("test@example.com", "Test", "User");
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("$2a$hashed");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            userService.register(request);

            // Assert
            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getPassword()).isEqualTo("$2a$hashed");
        }
    }

    // ─── login() ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("should return auth token on valid credentials")
        void shouldReturnToken_onValidCredentials() {
            // Arrange
            var user = buildUserWithPassword("user@example.com", "hashed");
            var request = new LoginRequest();
            request.setEmail("user@example.com");
            request.setPassword("rawPassword");

            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("rawPassword", "hashed")).thenReturn(true);
            when(jwtService.generateToken(any(), any(), any())).thenReturn("jwt-token");

            // Act
            var response = userService.login(request);

            // Assert
            assertThat(response.getToken()).isEqualTo("jwt-token");
            assertThat(response.getTokenType()).isEqualTo("Bearer");
            assertThat(response.getUser().getEmail()).isEqualTo("user@example.com");
        }

        @Test
        @DisplayName("should throw UserNotFoundException when email does not exist")
        void shouldThrow_whenEmailNotFound() {
            // Arrange
            var request = new LoginRequest();
            request.setEmail("ghost@example.com");
            request.setPassword("pass");
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("should throw UserNotFoundException on wrong password")
        void shouldThrow_whenPasswordWrong() {
            // Arrange
            var user = buildUserWithPassword("user@example.com", "hashed");
            var request = new LoginRequest();
            request.setEmail("user@example.com");
            request.setPassword("wrongPass");

            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrongPass", "hashed")).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("Invalid credentials");
        }
    }

    // ─── findById() ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("should return user when ID exists")
        void shouldReturnUser_whenIdExists() {
            var id = UUID.randomUUID();
            var user = User.builder().id(id).email("u@u.com").firstName("A").lastName("B").build();
            when(userRepository.findById(id)).thenReturn(Optional.of(user));

            var response = userService.findById(id);

            assertThat(response.getId()).isEqualTo(id);
        }

        @Test
        @DisplayName("should throw UserNotFoundException when ID not found")
        void shouldThrow_whenIdNotFound() {
            var id = UUID.randomUUID();
            when(userRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.findById(id))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private RegisterRequest buildRegisterRequest(String email, String first, String last) {
        var req = new RegisterRequest();
        req.setEmail(email);
        req.setPassword("password123");
        req.setFirstName(first);
        req.setLastName(last);
        return req;
    }

    private User buildUser(RegisterRequest req) {
        return User.builder()
                .id(UUID.randomUUID())
                .email(req.getEmail())
                .password("encoded")
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .build();
    }

    private User buildUserWithPassword(String email, String hashedPassword) {
        return User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .password(hashedPassword)
                .firstName("Test")
                .lastName("User")
                .role(User.Role.CUSTOMER)
                .build();
    }
}
