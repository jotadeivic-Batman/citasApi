package co.fcv.citas.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface RescheduleRequestJpaRepository extends JpaRepository<RescheduleRequestEntity, Long> {
    List<RescheduleRequestEntity> findByRequestedByUserId(Long userId);

    @Query("SELECT r FROM RescheduleRequestEntity r WHERE r.status.code = :statusCode ORDER BY r.createdAt ASC")
    List<RescheduleRequestEntity> findByStatusCode(String statusCode);
}
