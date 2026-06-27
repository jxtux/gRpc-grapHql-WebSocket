package com.demo.chatgateway.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Component
public class JwtUtil {
    @Value("${chat.security.jwt-secret}")
    private String jwtSecret;
    private final ObjectMapper mapper = new ObjectMapper();

    public Long validateAndGetUserId(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) throw new IllegalArgumentException("Token inválido");
            String expected = sign(parts[0] + "." + parts[1]);
            if (!expected.equals(parts[2])) throw new IllegalArgumentException("Firma inválida");
            Map<?, ?> payload = mapper.readValue(Base64.getUrlDecoder().decode(parts[1]), Map.class);
            Number exp = (Number) payload.get("exp");
            if (exp.longValue() < Instant.now().getEpochSecond()) throw new IllegalArgumentException("Token expirado");
            return Long.valueOf((String) payload.get("sub"));
        } catch (Exception e) {
            throw new IllegalArgumentException("JWT inválido", e);
        }
    }

    private String sign(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }
}
