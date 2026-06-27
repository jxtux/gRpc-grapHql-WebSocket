package com.demo.usuario.controller;

import com.demo.usuario.dto.*;
import com.demo.usuario.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) { this.userService = userService; }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) { return userService.login(request); }

    @GetMapping("/{id}")
    public UserDto getById(@PathVariable Long id) { return userService.getById(id); }

    @PostMapping("/batch")
    public List<UserDto> batch(@Valid @RequestBody UserBatchRequest request) { return userService.getBatch(request.ids()); }

    @GetMapping
    public List<UserDto> getAll() { return userService.getAll(); }

    @PostMapping
    public UserDto create(@Valid @RequestBody UserCreateUpdateRequest request) { return userService.create(request); }

    @PutMapping("/{id}")
    public UserDto update(@PathVariable Long id, @Valid @RequestBody UserCreateUpdateRequest request) {
        return userService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
