package com.example.ReactiveDemo.service;

import com.example.ReactiveDemo.controller.DTO.UserDTO;
import com.example.ReactiveDemo.repository.UserRepository;
import com.example.ReactiveDemo.repository.entities.UserEntity;
import com.example.ReactiveDemo.service.implementations.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@AllArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;

    // Function to create a user
    @Override
    public Mono<UserDTO> createUser(String email) {
        return userRepo.findByEmail(email)
                .hasElement()
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalStateException("Email already exists"));
                    }
                    return userRepo.save(
                            UserEntity.builder()
                                    .email(email)
                                    .build()
                    );
                })
                .map(user -> UserDTO.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .build());
    }


    // Function to find a user by UUID
    @Override
    public Mono<UserDTO> findUserById(UUID userId) {
        return userRepo.findById(userId)
                .map(user -> UserDTO.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .build()
                )
                .switchIfEmpty(
                        Mono.error(new RuntimeException("User not found with userId: " + userId))
                );
    }

    @Override
    public Mono<UserDTO> findUserByEmail(String email) {
        return userRepo.findByEmail(email)
                .map(user -> UserDTO.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .build()
                )
                .switchIfEmpty(
                        Mono.error(new RuntimeException("User not found with email: " + email))
                );
    }

}
