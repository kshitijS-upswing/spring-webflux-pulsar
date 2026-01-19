package com.example.ReactiveDemo.controller;

import com.example.ReactiveDemo.controller.requests.CreateUserRequest;
import com.example.ReactiveDemo.controller.DTO.UserDTO;
import com.example.ReactiveDemo.controller.implementations.UserController;
import com.example.ReactiveDemo.service.implementations.UserService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserControllerImpl implements UserController {

    private final UserService userService;

    // CREATE USER
    // POST /users (body)
    @Override
    @PostMapping
    public Mono<UserDTO> createUser(@RequestBody CreateUserRequest request) {
        return userService.createUser(request.getEmail());
    }

    // GET USER
    // POST /users/...
    @GetMapping("/{id}")
    public Mono<UserDTO> getUserById(@PathVariable UUID id) {
        return userService.findUserById(id);
    }

    // GET USER
    // POST /users/...
    @GetMapping
    public Mono<UserDTO> getUserByEmail(@RequestParam String email) {
        return userService.findUserByEmail(email);
    }

}

