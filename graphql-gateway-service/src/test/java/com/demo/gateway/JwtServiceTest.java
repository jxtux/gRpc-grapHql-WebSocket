package com.demo.gateway;

import com.demo.gateway.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JwtServiceTest {
    @Autowired JwtService jwtService;

    @Test
    void tokenDebeDevolverUserId() {
        String token = jwtService.generateToken(1L, "usuario1");
        assertThat(jwtService.validateAndGetUserId(token)).isEqualTo(1L);
    }
}
