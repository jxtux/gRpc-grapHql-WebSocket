package com.demo.usuario.service;

import com.demo.usuario.dto.*;
import com.demo.usuario.entity.UserEntity;
import com.demo.usuario.entity.UserProfileEntity;
import com.demo.usuario.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) { this.userRepository = userRepository; }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        log.info("Login solicitado para username={}", request.username());
        return userRepository.findByUsername(request.username())
                .filter(UserEntity::isEnabled)
                .filter(u -> passwordEncoder.matches(request.password(), u.getPassword()))
                .map(u -> new LoginResponse(true, "Autenticación exitosa", toDto(u)))
                .orElseGet(() -> new LoginResponse(false, "Usuario o contraseña inválidos", null));
    }

    @Transactional(readOnly = true)
    public UserDto getById(Long id) {
        return userRepository.findById(id).map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAll() {
        return userRepository.findAll().stream().map(this::toDto).sorted(Comparator.comparing(UserDto::id)).toList();
    }

    @Transactional(readOnly = true)
    public List<UserDto> getBatch(List<Long> ids) {
        log.info("Batch de usuarios solicitado: {}", ids);
        return userRepository.findByIdIn(ids).stream()
                .map(this::toDto)
                .sorted(Comparator.comparingInt(u -> ids.indexOf(u.id())))
                .toList();
    }

    @Transactional
    public UserDto create(UserCreateUpdateRequest request) {
        UserEntity user = new UserEntity();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password() == null ? "1234" : request.password()));
        user.setEnabled(true);
        UserProfileEntity profile = new UserProfileEntity();
        profile.setUser(user);
        profile.setFirstName(request.firstName());
        profile.setLastName(request.lastName());
        profile.setEmail(request.email());
        profile.setPhone(request.phone());
        profile.setProfession(request.profession());
        profile.setDescription(request.description());
        profile.setAvatarUrl(request.avatarUrl());
        user.setProfile(profile);
        return toDto(userRepository.save(user));
    }

    @Transactional
    public UserDto update(Long id, UserCreateUpdateRequest request) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
        user.setUsername(request.username());
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        UserProfileEntity p = user.getProfile();
        p.setFirstName(request.firstName());
        p.setLastName(request.lastName());
        p.setEmail(request.email());
        p.setPhone(request.phone());
        p.setProfession(request.profession());
        p.setDescription(request.description());
        p.setAvatarUrl(request.avatarUrl());
        return toDto(userRepository.save(user));
    }

    @Transactional
    public void delete(Long id) { userRepository.deleteById(id); }

    private UserDto toDto(UserEntity u) {
        UserProfileEntity p = u.getProfile();
        return new UserDto(u.getId(), u.getUsername(), p.getFirstName(), p.getLastName(), p.getEmail(),
                p.getPhone(), p.getProfession(), p.getDescription(), p.getAvatarUrl());
    }
}
