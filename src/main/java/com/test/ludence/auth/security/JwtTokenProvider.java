package com.test.ludence.auth.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.ludence.auth.domain.info.AuthErrorInfo;
import com.test.ludence.common.error.exception.BusinessException;
import com.test.ludence.user.domain.entity.User;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final byte[] secret;
    private final long expirationSeconds;

    public JwtTokenProvider(
            ObjectMapper objectMapper,
            Clock clock,
            @Value("${auth.jwt.secret}") String secret,
            @Value("${auth.jwt.expiration-seconds}") long expirationSeconds
    ) {
        this.objectMapper = objectMapper;
        this.clock = clock;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationSeconds = expirationSeconds;
    }

    public String createToken(User user) {
        if (user.getId() == null) {
            throw new IllegalArgumentException("저장되지 않은 회원은 토큰을 발급할 수 없습니다.");
        }

        Instant issuedAt = clock.instant();
        Map<String, Object> claims = Map.of(
                "sub", user.getId().toString(),
                "iat", issuedAt.getEpochSecond(),
                "exp", issuedAt.plusSeconds(expirationSeconds).getEpochSecond()
        );
        return createToken(claims);
    }

    public Long getUserId(String token) {
        try {
            String[] parts = token.split("\\.");
            validateParts(parts);
            validateSignature(parts);

            @SuppressWarnings("unchecked")
            Map<String, Object> claims = objectMapper.readValue(BASE64_URL_DECODER.decode(parts[1]), Map.class);
            validateExpiration(claims);
            return Long.valueOf((String) claims.get("sub"));
        } catch (Exception exception) {
            throw new BusinessException(AuthErrorInfo.INVALID_TOKEN);
        }
    }

    private String createToken(Map<String, Object> claims) {
        try {
            String header = encode(objectMapper.writeValueAsBytes(Map.of("alg", "HS256", "typ", "JWT")));
            String payload = encode(objectMapper.writeValueAsBytes(claims));
            String signature = encode(sign(header + "." + payload));
            return header + "." + payload + "." + signature;
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("JWT 생성에 실패했습니다.", exception);
        }
    }

    private void validateParts(String[] parts) {
        if (parts.length != 3) {
            throw new BusinessException(AuthErrorInfo.INVALID_TOKEN);
        }
    }

    private void validateSignature(String[] parts) {
        byte[] expected = sign(parts[0] + "." + parts[1]);
        byte[] actual = BASE64_URL_DECODER.decode(parts[2]);
        if (!MessageDigest.isEqual(expected, actual)) {
            throw new BusinessException(AuthErrorInfo.INVALID_TOKEN);
        }
    }

    private void validateExpiration(Map<String, Object> claims) {
        Number expiration = (Number) claims.get("exp");
        if (expiration == null || expiration.longValue() <= clock.instant().getEpochSecond()) {
            throw new BusinessException(AuthErrorInfo.INVALID_TOKEN);
        }
    }

    private byte[] sign(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
            return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("JWT 서명에 실패했습니다.", exception);
        }
    }

    private String encode(byte[] value) {
        return BASE64_URL_ENCODER.encodeToString(value);
    }
}
