package co.fcv.citas.domain;

import java.time.LocalDateTime;

public final class Insurance {
    private Insurance() {}

    public record Regime(Short id, String code, String name) {}

    public record EpsEntity(Long id, String code, String name, boolean active) {}

    public record Plan(Long id, Long epsId, Short regimeId, String code, String name, boolean active) {}

    public record UserAffiliation(
            Long id,
            Long userId,
            Long planId,
            String membershipNumber,
            boolean isCurrent,
            LocalDateTime createdAt
    ) {}
}
