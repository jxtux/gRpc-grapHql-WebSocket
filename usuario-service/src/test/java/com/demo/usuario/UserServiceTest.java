package com.demo.usuario;

import com.demo.usuario.dto.LoginRequest;
import com.demo.usuario.repository.UserRepository;
import com.demo.usuario.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserServiceTest {
    @Autowired UserService userService;
    @Autowired UserRepository userRepository;

    @Test
    void loginConCredencialesCorrectasDebeAutenticar() {
        var response = userService.login(new LoginRequest("usuario1", "1234"));
        assertThat(response.authenticated()).isTrue();
        assertThat(response.user().username()).isEqualTo("usuario1");
    }

    @Test
    void batchDebeRetornarUsuariosSolicitados() {
        var users = userService.getBatch(List.of(2L, 3L, 4L));
        assertThat(users).hasSize(3);
    }
}
