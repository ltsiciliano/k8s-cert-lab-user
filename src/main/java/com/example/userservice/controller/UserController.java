package com.example.userservice.controller;

import com.example.userservice.dto.GetUsersResponse;
import com.example.userservice.dto.UserRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.ExternalApiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "User Service", description = "Endpoints para gerenciamento de usuários")
public class UserController {

    private final UserRepository userRepository;
    private final ExternalApiService externalApiService;

    @Value("${WELCOME_MESSAGE:Hello CKAD}")
    private String welcomeMessage;

    @GetMapping("/users")
    @Operation(summary = "Lista todos os usuários", responses = {
            @ApiResponse(responseCode = "200", description = "Lista de usuários", content = @Content(schema = @Schema(implementation = GetUsersResponse.class)))
    })
    public ResponseEntity<GetUsersResponse> getUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponse> userResponses = users.stream().map(this::toUserResponse).collect(Collectors.toList());
        GetUsersResponse resp = GetUsersResponse.builder()
                .welcomeMessage(welcomeMessage)
                .users(userResponses)
                .build();
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/users")
    @Operation(summary = "Cria um novo usuário")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .cpf(request.getCpf())
                .build();
        User saved = userRepository.save(user);
        return new ResponseEntity<>(toUserResponse(saved), HttpStatus.CREATED);
    }

    @GetMapping("/users/cpf/{cpf}")
    @Operation(summary = "Busca usuário por CPF")
    public ResponseEntity<UserResponse> getUserByCpf(@PathVariable String cpf) {
        return userRepository.findByCpf(cpf)
                .map(user -> ResponseEntity.ok(toUserResponse(user)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    private UserResponse toUserResponse(User user) {
        JsonNode impostos = externalApiService.fetchImpostos(user.getCpf());
        JsonNode pagamentos = externalApiService.fetchPagamentos(user.getCpf());
        // ensure arrays
        JsonNodeFactory f = JsonNodeFactory.instance;
        ArrayNode impArray = impostos != null && impostos.isArray() ? (ArrayNode) impostos : f.arrayNode();
        ArrayNode pagArray = pagamentos != null && pagamentos.isArray() ? (ArrayNode) pagamentos : f.arrayNode();
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .cpf(user.getCpf())
                .impostos(impArray)
                .pagamentos(pagArray)
                .build();
    }
}
