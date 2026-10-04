package co.fcv.citas;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:citas_mvp;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.username=sa", "spring.datasource.password=", "spring.datasource.driver-class-name=org.h2.Driver"
})
@AutoConfigureMockMvc
class MvpIntegrationTest {
    static {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
    static final String ACCESS = UUID.randomUUID() + UUID.randomUUID().toString();
    static final String REFRESH = UUID.randomUUID() + UUID.randomUUID().toString();

    @DynamicPropertySource
    static void secrets(DynamicPropertyRegistry registry) {
        registry.add("app.jwt.access-secret", () -> ACCESS);
        registry.add("app.jwt.refresh-secret", () -> REFRESH);
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    String loginToken(String email, String password) throws Exception {
        var res = mvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content(json.writeValueAsBytes(Map.of("email", email, "password", password))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(res).path("accessToken").asText();
    }

    @Test
    void testRescheduleFlowLifecycle() throws Exception {
        String patientToken = loginToken("paciente@fcv.test", "User123*");
        String adminToken = loginToken("admin@fcv.test", "Admin123*");

        // 1. Agendar cita general inicial (2026-10-01 08:30:00)
        var bookReq = Map.of(
                "professionalId", 1,
                "locationId", 1,
                "specialtyId", 1,
                "startAt", "2026-10-01T08:30:00"
        );
        var createRes = mvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType("application/json")
                        .content(json.writeValueAsBytes(bookReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("statusCode").value("APPROVED"))
                .andReturn().getResponse().getContentAsString();
        long appointmentId = json.readTree(createRes).path("id").asLong();

        // 2. Solicitar reprogramación a 2026-10-01 09:00:00 (RF-15)
        var rescheduleReq = Map.of(
                "locationId", 1,
                "newStartAt", "2026-10-01T09:00:00"
        );
        var reschedRes = mvc.perform(post("/api/appointments/" + appointmentId + "/reschedule")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType("application/json")
                        .content(json.writeValueAsBytes(rescheduleReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("statusId").value(1)) // 1 = PENDING
                .andReturn().getResponse().getContentAsString();
        long rescheduleId = json.readTree(reschedRes).path("id").asLong();

        // 3. Admin lista reprogramaciones pendientes (RF-18)
        mvc.perform(get("/api/admin/reschedules")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + rescheduleId + ")].appointmentId").value(appointmentId));

        // 4. Admin rechaza con motivo obligatorio (RN-04)
        mvc.perform(patch("/api/admin/reschedules/" + rescheduleId + "/reject")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(json.writeValueAsBytes(Map.of("reason", "Agenda cerrada por mantenimiento"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("statusId").value(3)) // 3 = REJECTED
                .andExpect(jsonPath("rejectionReason").value("Agenda cerrada por mantenimiento"));

        // 5. Paciente solicita nueva reprogramación a 2026-10-01 09:30:00
        var reschedRes2 = mvc.perform(post("/api/appointments/" + appointmentId + "/reschedule")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType("application/json")
                        .content(json.writeValueAsBytes(Map.of("locationId", 1, "newStartAt", "2026-10-01T09:30:00"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long rescheduleId2 = json.readTree(reschedRes2).path("id").asLong();

        // 6. Admin aprueba la reprogramación (RF-15)
        mvc.perform(patch("/api/admin/reschedules/" + rescheduleId2 + "/approve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("statusId").value(2)); // 2 = APPROVED
    }

    @Test
    void testPasswordRecoveryAndReset() throws Exception {
        String email = "paciente@fcv.test";
        // 1. Solicitar token de recuperación (RF-03)
        var forgotRes = mvc.perform(post("/api/auth/forgot-password")
                        .contentType("application/json")
                        .content(json.writeValueAsBytes(Map.of("email", email))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("resetToken").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        String token = json.readTree(forgotRes).path("resetToken").asText();

        // 2. Restablecer contraseña con el token
        String newPassword = "NewSecurePass2026*";
        mvc.perform(post("/api/auth/reset-password")
                        .contentType("application/json")
                        .content(json.writeValueAsBytes(Map.of("token", token, "newPassword", newPassword))))
                .andExpect(status().isOk());

        // 3. Login con la nueva contraseña
        String newToken = loginToken(email, newPassword);
        assertThat(newToken).isNotBlank();

        // 4. Token ya usado no puede reutilizarse (un solo uso)
        mvc.perform(post("/api/auth/reset-password")
                        .contentType("application/json")
                        .content(json.writeValueAsBytes(Map.of("token", token, "newPassword", "AnotherPass123*"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testInsuranceAndAffiliations() throws Exception {
        String patientToken = loginToken("paciente@fcv.test", "User123*");
        String adminToken = loginToken("admin@fcv.test", "Admin123*");

        // 1. Consultar regímenes y EPS públicas (RF-05, RF-06)
        mvc.perform(get("/api/catalogs/regimes")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("CONTRIBUTIVO"));

        mvc.perform(get("/api/catalogs/eps")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("SANITAS"));

        // 2. Paciente se afilia a EPS Sanitas (Plan 1) (RF-04)
        mvc.perform(post("/api/users/me/affiliation")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType("application/json")
                        .content(json.writeValueAsBytes(Map.of(
                                "planId", 1,
                                "membershipNumber", "AFIL-PAC-999888"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("membershipNumber").value("AFIL-PAC-999888"));

        // 3. Consultar afiliación actual
        mvc.perform(get("/api/users/me/affiliation")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("membershipNumber").value("AFIL-PAC-999888"));

        // 4. Admin crea una nueva EPS (RF-06)
        mvc.perform(post("/api/admin/eps")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(json.writeValueAsBytes(Map.of(
                                "code", "COMPENSAR",
                                "name", "Compensar EPS",
                                "active", true))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("code").value("COMPENSAR"));
    }
}
