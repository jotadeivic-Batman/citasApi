package co.fcv.citas.adapter.web;

import co.fcv.citas.application.SchedulingService;
import co.fcv.citas.domain.RescheduleRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RescheduleController {
    private final SchedulingFacade facade;

    public RescheduleController(SchedulingFacade facade) {
        this.facade = facade;
    }

    public record RequestRescheduleDto(
            @NotNull Short locationId,
            @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newStartAt
    ) {}

    public record RejectRescheduleDto(@NotBlank String reason) {}

    // Paciente solicita reprogramación de su cita aprobada
    @PostMapping("/appointments/{id}/reschedule")
    public ResponseEntity<RescheduleRequest> requestReschedule(
            @PathVariable Long id,
            @Valid @RequestBody RequestRescheduleDto req,
            Authentication auth) {
        Long userId = extractUserId(auth);
        var res = facade.requestReschedule(new SchedulingService.RequestRescheduleCommand(
                id, userId, req.locationId(), req.newStartAt()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    // Paciente consulta sus reprogramaciones
    @GetMapping("/appointments/my-reschedules")
    public List<RescheduleRequest> getMyReschedules(Authentication auth) {
        Long userId = extractUserId(auth);
        return facade.getMyReschedules(userId);
    }

    // Admin consulta reprogramaciones pendientes
    @GetMapping("/admin/reschedules")
    public List<RescheduleRequest> getPendingReschedules() {
        return facade.getPendingReschedules();
    }

    // Admin aprueba reprogramación
    @PatchMapping("/admin/reschedules/{id}/approve")
    public RescheduleRequest approveReschedule(@PathVariable Long id, Authentication auth) {
        Long adminUserId = extractUserId(auth);
        return facade.approveReschedule(id, adminUserId);
    }

    // Admin rechaza reprogramación con motivo
    @PatchMapping("/admin/reschedules/{id}/reject")
    public RescheduleRequest rejectReschedule(
            @PathVariable Long id,
            @Valid @RequestBody RejectRescheduleDto req,
            Authentication auth) {
        Long adminUserId = extractUserId(auth);
        return facade.rejectReschedule(id, adminUserId, req.reason());
    }

    private Long extractUserId(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
            throw new IllegalArgumentException("No autenticado");
        }
        return Long.parseLong(jwt.getSubject());
    }
}
