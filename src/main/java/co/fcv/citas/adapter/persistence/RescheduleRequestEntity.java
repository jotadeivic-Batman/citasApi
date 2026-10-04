package co.fcv.citas.adapter.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reschedule_requests")
public class RescheduleRequestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private AppointmentEntity appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by_user_id", nullable = false)
    private UserEntity requestedByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_location_id", nullable = false)
    private LocationEntity requestedLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private RescheduleRequestStatusEntity status;

    @Column(name = "requested_start_at", nullable = false)
    private LocalDateTime requestedStartAt;

    @Column(name = "requested_end_at", nullable = false)
    private LocalDateTime requestedEndAt;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public RescheduleRequestEntity() {}

    public RescheduleRequestEntity(Long id, AppointmentEntity appointment, UserEntity requestedByUser,
                                   LocationEntity requestedLocation, RescheduleRequestStatusEntity status,
                                   LocalDateTime requestedStartAt, LocalDateTime requestedEndAt,
                                   String rejectionReason, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.appointment = appointment;
        this.requestedByUser = requestedByUser;
        this.requestedLocation = requestedLocation;
        this.status = status;
        this.requestedStartAt = requestedStartAt;
        this.requestedEndAt = requestedEndAt;
        this.rejectionReason = rejectionReason;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public AppointmentEntity getAppointment() { return appointment; }
    public UserEntity getRequestedByUser() { return requestedByUser; }
    public LocationEntity getRequestedLocation() { return requestedLocation; }
    public RescheduleRequestStatusEntity getStatus() { return status; }
    public LocalDateTime getRequestedStartAt() { return requestedStartAt; }
    public LocalDateTime getRequestedEndAt() { return requestedEndAt; }
    public String getRejectionReason() { return rejectionReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setStatus(RescheduleRequestStatusEntity status) { this.status = status; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}
