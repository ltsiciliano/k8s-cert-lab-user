package com.example.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest {

    @Schema(example = "Maria Silva")
    @NotBlank(message = "name is required")
    private String name;

    @Schema(example = "maria@example.com")
    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    private String email;

    @Schema(example = "12345678900")
    @NotBlank(message = "cpf is required")
    private String cpf;
}
