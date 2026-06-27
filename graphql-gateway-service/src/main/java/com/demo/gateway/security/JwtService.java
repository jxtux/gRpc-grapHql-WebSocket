package com.demo.gateway.security;

import com.demo.gateway.config.AppProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class JwtService {
    private final AppProperties properties;
    private final ObjectMapper mapper = new ObjectMapper();

    public JwtService(AppProperties properties) { this.properties = properties; }

    public String generateToken(Long userId, String username) {
        try {
            Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sub", String.valueOf(userId));
            payload.put("username", username);
            payload.put("iat", Instant.now().getEpochSecond());
            payload.put("exp", Instant.now().plusSeconds(3600).getEpochSecond());
            String h = b64(mapper.writeValueAsBytes(header));
            String p = b64(mapper.writeValueAsBytes(payload));
            String signature = sign(h + "." + p);
            return h + "." + p + "." + signature;
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar JWT", e);
        }
    }

    public Long validateAndGetUserId(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) throw new IllegalArgumentException("Token inválido");
            String expected = sign(parts[0] + "." + parts[1]);
            if (!expected.equals(parts[2])) throw new IllegalArgumentException("Firma JWT inválida");
            byte[] payloadJson = Base64.getUrlDecoder().decode(parts[1]);
            Map<?, ?> payload = mapper.readValue(payloadJson, Map.class);
            Number exp = (Number) payload.get("exp");
            if (exp.longValue() < Instant.now().getEpochSecond()) throw new IllegalArgumentException("Token expirado");
            return Long.valueOf((String) payload.get("sub"));
        } catch (Exception e) {
            throw new IllegalArgumentException("Token inválido", e);
        }
    }

    private String sign(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(properties.getJwtSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return b64(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    private String b64(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
