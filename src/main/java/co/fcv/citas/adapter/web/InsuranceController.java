package co.fcv.citas.adapter.web;

import co.fcv.citas.domain.Insurance;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class InsuranceController {
    private final SchedulingFacade facade;

    public InsuranceController(SchedulingFacade facade) {
        this.facade = facade;
    }

    public record SaveEpsRequest(Long id, @NotBlank String code, @NotBlank String name, Boolean active) {}
    public record SavePlanRequest(Long id, @NotNull Long epsId, @NotNull Short regimeId, @NotBlank String code, @NotBlank String name, Boolean active) {}
    public record AffiliateRequest(@NotNull Long planId, @NotBlank String membershipNumber) {}

    // --- Catálogos Públicos ---
    @GetMapping("/catalogs/regimes")
    public List<Insurance.Regime> getRegimes() {
        return facade.getInsuranceRegimes();
    }

    @GetMapping("/catalogs/eps")
    public List<Insurance.EpsEntity> getActiveEps() {
        return facade.getEps(true);
    }

    @GetMapping("/catalogs/eps/{epsId}/plans")
    public List<Insurance.Plan> getActivePlansByEps(@PathVariable Long epsId) {
        return facade.getPlans(epsId, true);
    }

    // --- Administración de Catálogos (ADMIN) ---
    @GetMapping("/admin/eps")
    public List<Insurance.EpsEntity> getAllEps() {
        return facade.getEps(false);
    }

    @PostMapping("/admin/eps")
    public ResponseEntity<Insurance.EpsEntity> saveEps(@Valid @RequestBody SaveEpsRequest req) {
        var eps = facade.saveEps(new Insurance.EpsEntity(req.id(), req.code().strip(), req.name().strip(), req.active() == null || req.active()));
        return ResponseEntity.status(req.id() == null ? HttpStatus.CREATED : HttpStatus.OK).body(eps);
    }

    @GetMapping("/admin/eps/plans")
    public List<Insurance.Plan> getAllPlans(@RequestParam(required = false) Long epsId) {
        return facade.getPlans(epsId, false);
    }

    @PostMapping("/admin/eps/plans")
    public ResponseEntity<Insurance.Plan> savePlan(@Valid @RequestBody SavePlanRequest req) {
        var plan = facade.savePlan(new Insurance.Plan(req.id(), req.epsId(), req.regimeId(), req.code().strip(), req.name().strip(), req.active() == null || req.active()));
        return ResponseEntity.status(req.id() == null ? HttpStatus.CREATED : HttpStatus.OK).body(plan);
    }

    // --- Afiliaciones de Usuario ---
    @GetMapping("/users/me/affiliation")
    public ResponseEntity<?> getMyAffiliation(Authentication auth) {
        Long userId = extractUserId(auth);
        return facade.getUserAffiliation(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/users/me/affiliation")
    public ResponseEntity<Insurance.UserAffiliation> affiliateMe(@Valid @RequestBody AffiliateRequest req, Authentication auth) {
        Long userId = extractUserId(auth);
        var affiliation = facade.affiliateUser(userId, req.planId(), req.membershipNumber());
        return ResponseEntity.status(HttpStatus.CREATED).body(affiliation);
    }

    private Long extractUserId(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
            throw new IllegalArgumentException("No autenticado");
        }
        return Long.parseLong(jwt.getSubject());
    }
}
