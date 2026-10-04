package co.fcv.citas.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RescheduleRequestStatusJpaRepository extends JpaRepository<RescheduleRequestStatusEntity, Short> {
    Optional<RescheduleRequestStatusEntity> findByCode(String code);
}
