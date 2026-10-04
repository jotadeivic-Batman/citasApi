package co.fcv.citas.adapter.persistence;

import co.fcv.citas.application.SchedulingPorts.PasswordResets;
import co.fcv.citas.domain.PasswordResetToken;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class PasswordResetPersistenceAdapter implements PasswordResets {
    private final PasswordResetTokenJpaRepository repo;
    private final UserJpaRepository userRepo;

    public PasswordResetPersistenceAdapter(PasswordResetTokenJpaRepository repo, UserJpaRepository userRepo) {
        this.repo = repo;
        this.userRepo = userRepo;
    }

    @Override
    public PasswordResetToken save(PasswordResetToken token) {
        var user = userRepo.findById(token.userId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        var entity = new PasswordResetTokenEntity(
                token.id(), user, token.tokenHash(), token.expiresAt(), token.usedAt(), token.createdAt()
        );
        var saved = repo.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<PasswordResetToken> findByTokenHash(String tokenHash) {
        return repo.findByTokenHash(tokenHash).map(this::toDomain);
    }

    @Override
    public void markUsed(Long tokenId) {
        repo.findById(tokenId).ifPresent(t -> {
            t.setUsedAt(LocalDateTime.now());
            repo.save(t);
        });
    }

    private PasswordResetToken toDomain(PasswordResetTokenEntity e) {
        return new PasswordResetToken(
                e.getId(),
                e.getUser().getId(),
                e.getTokenHash(),
                e.getExpiresAt(),
                e.getUsedAt(),
                e.getCreatedAt()
        );
    }
}
