package co.fcv.citas.adapter.web;

import co.fcv.citas.application.SchedulingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {
    private final SchedulingFacade facade;

    public PasswordResetController(SchedulingFacade facade) {
        this.facade = facade;
    }

    public record ForgotPasswordRequest(@NotBlank @Email String email) {}
    public record ResetPasswordRequest(@NotBlank String token, @NotBlank @Size(min = 6) String newPassword) {}

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        var res = facade.requestPasswordReset(req.email());
        return ResponseEntity.ok(Map.of(
                "message", "Si el correo está registrado, se ha generado el enlace de recuperación.",
                "resetToken", res.resetToken(), // Expuesto para facilitar pruebas y flujo en laboratorio académico
                "expiresAt", res.expiresAt()
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        facade.resetPassword(req.token(), req.newPassword());
        return ResponseEntity.ok(Map.of("message", "Contraseña restablecida exitosamente."));
    }
}
