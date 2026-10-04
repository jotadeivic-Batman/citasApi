package co.fcv.citas.adapter.persistence;

import co.fcv.citas.application.SchedulingPorts.RescheduleRequests;
import co.fcv.citas.domain.RescheduleRequest;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
public class RescheduleRequestPersistenceAdapter implements RescheduleRequests {
    private final RescheduleRequestJpaRepository repo;
    private final AppointmentJpaRepository appointmentRepo;
    private final UserJpaRepository userRepo;
    private final LocationJpaRepository locationRepo;
    private final RescheduleRequestStatusJpaRepository statusRepo;

    public RescheduleRequestPersistenceAdapter(RescheduleRequestJpaRepository repo,
                                              AppointmentJpaRepository appointmentRepo,
                                              UserJpaRepository userRepo,
                                              LocationJpaRepository locationRepo,
                                              RescheduleRequestStatusJpaRepository statusRepo) {
        this.repo = repo;
        this.appointmentRepo = appointmentRepo;
        this.userRepo = userRepo;
        this.locationRepo = locationRepo;
        this.statusRepo = statusRepo;
    }

    @Override
    public RescheduleRequest save(RescheduleRequest req) {
        var appointment = appointmentRepo.findById(req.appointmentId())
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));
        var user = userRepo.findById(req.requestedByUserId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        var location = locationRepo.findById(req.requestedLocationId())
                .orElseThrow(() -> new IllegalArgumentException("Sede no encontrada"));
        var status = statusRepo.findById(req.statusId())
                .orElseThrow(() -> new IllegalArgumentException("Estado de reprogramación no encontrado"));

        var entity = new RescheduleRequestEntity(
                req.id(), appointment, user, location, status,
                req.requestedStartAt(), req.requestedEndAt(),
                req.rejectionReason(), req.createdAt(), req.updatedAt()
        );
        var saved = repo.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<RescheduleRequest> findById(Long id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public List<RescheduleRequest> findByUserId(Long userId) {
        return repo.findByRequestedByUserId(userId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<RescheduleRequest> findPending() {
        return repo.findByStatusCode("PENDING").stream().map(this::toDomain).toList();
    }

    private RescheduleRequest toDomain(RescheduleRequestEntity e) {
        return new RescheduleRequest(
                e.getId(),
                e.getAppointment().getId(),
                e.getRequestedByUser().getId(),
                e.getRequestedLocation().getId(),
                e.getStatus().getId(),
                e.getRequestedStartAt(),
                e.getRequestedEndAt(),
                e.getRejectionReason(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
