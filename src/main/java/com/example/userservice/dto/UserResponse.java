package com.example.userservice.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    @Schema(example = "1")
    private Long id;
    @Schema(example = "Maria Silva")
    private String name;
    @Schema(example = "maria@example.com")
    private String email;
    @Schema(example = "12345678900")
    private String cpf;

    @Schema(description = "Lista de impostos do usuário (de API externa)")
    private JsonNode impostos; // always an array

    @Schema(description = "Lista de pagamentos do usuário (de API externa)")
    private JsonNode pagamentos; // always an array
}
