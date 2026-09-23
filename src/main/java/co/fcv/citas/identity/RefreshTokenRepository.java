package co.fcv.citas.identity;
import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity,Long>{ Optional<RefreshTokenEntity> findByTokenHash(String hash); }
