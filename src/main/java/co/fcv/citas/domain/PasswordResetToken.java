package co.fcv.citas.domain;

import java.time.LocalDateTime;

public record PasswordResetToken(
        Long id,
        Long userId,
        String tokenHash,
        LocalDateTime expiresAt,
        LocalDateTime usedAt,
        LocalDateTime createdAt
) {
    public boolean isValid(LocalDateTime now) {
        return usedAt == null && expiresAt.isAfter(now);
    }
}
