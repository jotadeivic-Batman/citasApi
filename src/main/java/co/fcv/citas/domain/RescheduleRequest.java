package co.fcv.citas.domain;

import java.time.LocalDateTime;

public record RescheduleRequest(
        Long id,
        Long appointmentId,
        Long requestedByUserId,
        Short requestedLocationId,
        Short statusId,
        LocalDateTime requestedStartAt,
        LocalDateTime requestedEndAt,
        String rejectionReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static final String PENDING = "PENDING";
    public static final String APPROVED = "APPROVED";
    public static final String REJECTED = "REJECTED";
}
