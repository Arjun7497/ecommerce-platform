package com.ecommerce.user.dto;

import com.ecommerce.user.model.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserResponse {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private User.Role role;
    private boolean active;
    private LocalDateTime createdAt;
}
