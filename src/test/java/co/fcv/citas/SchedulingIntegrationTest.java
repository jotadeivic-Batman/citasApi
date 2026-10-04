package co.fcv.citas;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:citas_sched;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.username=sa", "spring.datasource.password=", "spring.datasource.driver-class-name=org.h2.Driver"
})
@AutoConfigureMockMvc
class SchedulingIntegrationTest {
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
    void testCatalogsArePublic() throws Exception {
        mvc.perform(get("/api/catalogs/locations")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("HIC"))
                .andExpect(jsonPath("$[1].code").value("ICV"));

        mvc.perform(get("/api/catalogs/specialties")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("MED_GENERAL"))
                .andExpect(jsonPath("$[1].code").value("CARDIOLOGIA"));
    }

    @Test
    void availabilityReturnsOneOrTwoConsecutiveSlotsBySpecialtyDuration() throws Exception {
        String generalDoctorToken = loginToken("dr.mendoza@fcv.test", "Doc123*");
        String cardiologistToken = loginToken("dra.castro@fcv.test", "Doc123*");
                String patientToken = loginToken("paciente@fcv.test", "User123*");
        mvc.perform(post("/api/professionals/1/blocks")
                        .header("Authorization", "Bearer " + generalDoctorToken)
                        .contentType("application/json")
                        .content(json.writeValueAsBytes(Map.of(
                                "locationId", 1,
                                "date", "2026-10-15",
                                "startTime", "08:00",
                                "endTime", "10:00"))))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/professionals/2/blocks")
                        .header("Authorization", "Bearer " + cardiologistToken)
                        .contentType("application/json")
                        .content(json.writeValueAsBytes(Map.of(
                                "locationId", 1,
                                "date", "2026-10-15",
                                "startTime", "14:00",
                                "endTime", "18:00"))))
                .andExpect(status().isCreated());

        var generalResponse = mvc.perform(get("/api/availability")
                        .param("locationId", "1")
                        .param("specialtyId", "1")
                        .param("professionalId", "1")
                        .param("date", "2026-10-15"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode generalSlots = json.readTree(generalResponse);

        assertThat(generalSlots).hasSize(4);
        assertThat(LocalDateTime.parse(generalSlots.get(0).path("endAt").asText())
                .minusMinutes(30)).isEqualTo(LocalDateTime.parse(generalSlots.get(0).path("startAt").asText()));

        var generalBooking = Map.of(
                "professionalId", 1,
                "locationId", 1,
                "specialtyId", 1,
                "startAt", "2026-10-15T08:00:00"
        );
        mvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType("application/json")
                        .content(json.writeValueAsBytes(generalBooking)))
                .andExpect(status().isCreated());
        var remainingGeneralSlots = json.readTree(mvc.perform(get("/api/availability")
                        .param("locationId", "1")
                        .param("specialtyId", "1")
                        .param("professionalId", "1")
                        .param("date", "2026-10-15"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        assertThat(remainingGeneralSlots).hasSize(3);
        assertThat(remainingGeneralSlots.toString()).doesNotContain("2026-10-15T08:00:00");

        var cardiologyResponse = mvc.perform(get("/api/availability")
                        .param("locationId", "1")
                        .param("specialtyId", "2")
                        .param("professionalId", "2")
                        .param("date", "2026-10-15"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode cardiologySlots = json.readTree(cardiologyResponse);

        assertThat(cardiologySlots).hasSize(7);
        assertThat(LocalDateTime.parse(cardiologySlots.get(0).path("startAt").asText()))
                .isEqualTo(LocalDateTime.parse("2026-10-15T14:00:00"));
        assertThat(LocalDateTime.parse(cardiologySlots.get(0).path("endAt").asText())
                .minusMinutes(60)).isEqualTo(LocalDateTime.parse(cardiologySlots.get(0).path("startAt").asText()));
        assertThat(LocalDateTime.parse(cardiologySlots.get(4).path("startAt").asText()))
                .isEqualTo(LocalDateTime.parse("2026-10-15T16:00:00"));
        assertThat(LocalDateTime.parse(cardiologySlots.get(6).path("startAt").asText()))
                .isEqualTo(LocalDateTime.parse("2026-10-15T17:00:00"));

        var specializedBooking = Map.of(
                "professionalId", 2,
                "locationId", 1,
                "specialtyId", 2,
                "startAt", "2026-10-15T14:00:00"
        );
        mvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType("application/json")
                        .content(json.writeValueAsBytes(specializedBooking)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("statusCode").value("REQUESTED"));
        var remainingCardiologySlots = json.readTree(mvc.perform(get("/api/availability")
                        .param("locationId", "1")
                        .param("specialtyId", "2")
                        .param("professionalId", "2")
                        .param("date", "2026-10-15"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        assertThat(remainingCardiologySlots).hasSize(5);
        assertThat(remainingCardiologySlots.toString()).doesNotContain("2026-10-15T14:00:00");

        var pastSlots = json.readTree(mvc.perform(get("/api/availability")
                        .param("locationId", "1")
                        .param("specialtyId", "1")
                        .param("professionalId", "1")
                        .param("date", "2026-09-25"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        assertThat(pastSlots).isEmpty();
    }

    @Test
    void concurrentPatientsCannotBookTheSameSlot() throws Exception {
        String firstPatientToken = loginToken("paciente@fcv.test", "User123*");
        String userId = UUID.randomUUID().toString().replace("-", "");
        var secondPatient = Map.of(
                "firstName", "Paciente",
                "lastName", "Concurrente",
                "documentType", "CC",
                "documentNumber", userId.substring(0, 20),
                "email", userId + "@example.test",
                "phone", "3000000000",
                "password", "User123*"
        );
        mvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(json.writeValueAsBytes(secondPatient)))
                .andExpect(status().isCreated());
        String secondPatientToken = loginToken(secondPatient.get("email"), secondPatient.get("password"));
        var booking = Map.of(
                "professionalId", 1,
                "locationId", 1,
                "specialtyId", 1,
                "startAt", "2026-10-01T10:00:00"
        );

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            Callable<Integer> firstBooking = () -> submitBooking(firstPatientToken, booking, ready, start);
            Callable<Integer> secondBooking = () -> submitBooking(secondPatientToken, booking, ready, start);
            Future<Integer> firstResult = pool.submit(firstBooking);
            Future<Integer> secondResult = pool.submit(secondBooking);

            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            assertThat(List.of(firstResult.get(10, TimeUnit.SECONDS), secondResult.get(10, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(201, 409);
        }
    }

        private Integer submitBooking(String token, Map<String, ?> booking,
                                   CountDownLatch ready, CountDownLatch start) throws Exception {
        ready.countDown();
        if (!start.await(5, TimeUnit.SECONDS)) throw new IllegalStateException("Concurrent booking start timed out");
        return mvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(json.writeValueAsBytes(booking)))
                .andReturn().getResponse().getStatus();
    }

    @Test
    void testGeneralAppointmentIsAutoApprovedAndCannotBeDoubleBooked() throws Exception {
        String patientToken = loginToken("paciente@fcv.test", "User123*");

        // Doctor Mendoza (id 1, Medicina General, HIC = id 1), slot 2026-10-01 08:00:00
        var bookRequest = Map.of(
                "professionalId", 1,
                "locationId", 1,
                "specialtyId", 1,
                "startAt", "2026-10-01T08:00:00"
        );

        // First booking -> APPROVED (RN-02)
        var res = mvc.perform(post("/api/appointments")
                .header("Authorization", "Bearer " + patientToken)
                .contentType("application/json")
                .content(json.writeValueAsBytes(bookRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("statusCode").value("APPROVED"))
                .andReturn().getResponse().getContentAsString();

        JsonNode created = json.readTree(res);
        long appointmentId = created.path("id").asLong();

        // Second booking on same slot -> CONFLICT 409 (RN-01 Anti-double booking)
        mvc.perform(post("/api/appointments")
                .header("Authorization", "Bearer " + patientToken)
                .contentType("application/json")
                .content(json.writeValueAsBytes(bookRequest)))
                .andExpect(status().isConflict());

        // Patient can view my-appointments
        mvc.perform(get("/api/appointments/my-appointments")
                .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + appointmentId + ")].statusCode").value("APPROVED"));

        // Patient can cancel appointment (RF-14)
        mvc.perform(patch("/api/appointments/" + appointmentId + "/cancel")
                .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("statusCode").value("CANCELLED"));

        // Slot is released after cancellation (RN-09) -> Booking again should succeed!
        mvc.perform(post("/api/appointments")
                .header("Authorization", "Bearer " + patientToken)
                .contentType("application/json")
                .content(json.writeValueAsBytes(bookRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("statusCode").value("APPROVED"));
    }

    @Test
    void testSpecializedAppointmentRequiresAdminApprovalAndRejectionRequiresReason() throws Exception {
        String patientToken = loginToken("paciente@fcv.test", "User123*");
        String adminToken = loginToken("admin@fcv.test", "Admin123*");

        // Dra. Castro (id 2, Cardiología = id 2, HIC = id 1, slot 2026-10-01 14:00:00, 60 mins)
        var bookRequest = Map.of(
                "professionalId", 2,
                "locationId", 1,
                "specialtyId", 2,
                "startAt", "2026-10-01T14:00:00"
        );

        // Specialized booking -> REQUESTED (RN-03)
        var res = mvc.perform(post("/api/appointments")
                .header("Authorization", "Bearer " + patientToken)
                .contentType("application/json")
                .content(json.writeValueAsBytes(bookRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("statusCode").value("REQUESTED"))
                .andReturn().getResponse().getContentAsString();

        long appointmentId = json.readTree(res).path("id").asLong();

        // Non-admin cannot approve
        mvc.perform(patch("/api/admin/appointments/" + appointmentId + "/approve")
                .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden());

        // Admin rejects without reason -> 400 Bad Request (RN-04)
        mvc.perform(patch("/api/admin/appointments/" + appointmentId + "/reject")
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .content(json.writeValueAsBytes(Map.of("reason", "  "))))
                .andExpect(status().isBadRequest());

        // Admin rejects with reason -> REJECTED and slots released (RN-04, RN-09)
        mvc.perform(patch("/api/admin/appointments/" + appointmentId + "/reject")
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .content(json.writeValueAsBytes(Map.of("reason", "No cumple requisitos de remisión previa"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("statusCode").value("REJECTED"))
                .andExpect(jsonPath("rejectionReason").value("No cumple requisitos de remisión previa"));

        // Verify slot is freed after rejection: book again!
        var res2 = mvc.perform(post("/api/appointments")
                .header("Authorization", "Bearer " + patientToken)
                .contentType("application/json")
                .content(json.writeValueAsBytes(bookRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("statusCode").value("REQUESTED"))
                .andReturn().getResponse().getContentAsString();

        long appointmentId2 = json.readTree(res2).path("id").asLong();

        // Admin approves -> APPROVED
        mvc.perform(patch("/api/admin/appointments/" + appointmentId2 + "/approve")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("statusCode").value("APPROVED"));
    }

    @Test
    void testProfessionalManagementAndListing() throws Exception {
        String patientToken = loginToken("paciente@fcv.test", "User123*");
        String adminToken = loginToken("admin@fcv.test", "Admin123*");

        // Professionals can be listed publicly or by any user
        mvc.perform(get("/api/professionals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$[0].firstName").value("Carlos"))
                .andExpect(jsonPath("$[0].professionalCode").value("PROF-GEN-001"));

        // Non-admin cannot register professional
        Map<String, Object> newProf = new HashMap<>();
        newProf.put("firstName", "Alberto");
        newProf.put("lastName", "Gómez");
        newProf.put("documentType", "CC");
        newProf.put("documentNumber", "1098765432");
        newProf.put("email", "alberto.gomez@fcv.test");
        newProf.put("phone", "+57 300 999 8877");
        newProf.put("password", "DocPass123*");
        newProf.put("professionalCode", "PROF-CARDIO-999");
        newProf.put("licenseNumber", "MP-99999-COL");
        newProf.put("specialtyIds", List.of(2));
        newProf.put("locationIds", List.of(1));

        mvc.perform(post("/api/professionals")
                .header("Authorization", "Bearer " + patientToken)
                .contentType("application/json")
                .content(json.writeValueAsBytes(newProf)))
                .andExpect(status().isForbidden());

        // Admin can register professional (RF-07)
        mvc.perform(post("/api/professionals")
                .header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .content(json.writeValueAsBytes(newProf)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("professionalCode").value("PROF-CARDIO-999"))
                .andExpect(jsonPath("firstName").value("Alberto"))
                .andExpect(jsonPath("specialties[0].code").value("CARDIOLOGIA"));

        // Verified in list
        mvc.perform(get("/api/professionals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.professionalCode == 'PROF-CARDIO-999')].firstName").value("Alberto"));
    }

    @Test
    void testSixtyMinuteSpecialtyRequiresTwoConsecutiveSlotsAndRejectsIsolatedSlots() throws Exception {
        String cardiologistToken = loginToken("dra.castro@fcv.test", "Doc123*");
        String generalDoctorToken = loginToken("dr.mendoza@fcv.test", "Doc123*");

        // Crear un bloque de exactamente 30 minutos (1 slot aislado) para el 2026-10-20 (11:00 a 11:30)
        mvc.perform(post("/api/professionals/2/blocks")
                        .header("Authorization", "Bearer " + cardiologistToken)
                        .contentType("application/json")
                        .content(json.writeValueAsBytes(Map.of(
                                "locationId", 1,
                                "date", "2026-10-20",
                                "startTime", "11:00",
                                "endTime", "11:30"))))
                .andExpect(status().isCreated());

        // Para Cardiología (60 min = 2 slots requeridos): NO debe aparecer disponibilidad (0 slots)
        var cardioRes = mvc.perform(get("/api/availability")
                        .param("locationId", "1")
                        .param("specialtyId", "2")
                        .param("professionalId", "2")
                        .param("date", "2026-10-20"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode cardioSlots = json.readTree(cardioRes);
        assertThat(cardioSlots).isEmpty();

        // Para Medicina General (30 min) con el Dr. Mendoza, si creamos un bloque de 30 min, SÍ aparece
        mvc.perform(post("/api/professionals/1/blocks")
                        .header("Authorization", "Bearer " + generalDoctorToken)
                        .contentType("application/json")
                        .content(json.writeValueAsBytes(Map.of(
                                "locationId", 1,
                                "date", "2026-10-20",
                                "startTime", "11:00",
                                "endTime", "11:30"))))
                .andExpect(status().isCreated());

        var genRes = mvc.perform(get("/api/availability")
                        .param("locationId", "1")
                        .param("specialtyId", "1")
                        .param("professionalId", "1")
                        .param("date", "2026-10-20"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode genSlots = json.readTree(genRes);
        assertThat(genSlots).hasSize(1);
        assertThat(genSlots.get(0).path("startAt").asText()).isEqualTo("2026-10-20T11:00:00");
    }

    @Test
    void testBusinessRulesValidationRN06AndRN08() throws Exception {
        String patientToken = loginToken("paciente@fcv.test", "User123*");
        String cardiologistToken = loginToken("dra.castro@fcv.test", "Doc123*");

        // RN-06: No se pueden crear bloques en el pasado
        mvc.perform(post("/api/professionals/2/blocks")
                        .header("Authorization", "Bearer " + cardiologistToken)
                        .contentType("application/json")
                        .content(json.writeValueAsBytes(Map.of(
                                "locationId", 1,
                                "date", "2020-01-01",
                                "startTime", "08:00",
                                "endTime", "10:00"))))
                .andExpect(status().isBadRequest());

        // RN-06: No se pueden reservar citas en el pasado
        mvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType("application/json")
                        .content(json.writeValueAsBytes(Map.of(
                                "professionalId", 1,
                                "locationId", 1,
                                "specialtyId", 1,
                                "startAt", "2020-01-01T08:00:00"))))
                .andExpect(status().isBadRequest());

        // RN-08: Intentar agendar con especialidad no asignada al profesional (Dr. Mendoza no es Cardiólogo)
        mvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType("application/json")
                        .content(json.writeValueAsBytes(Map.of(
                                "professionalId", 1,
                                "locationId", 1,
                                "specialtyId", 2,
                                "startAt", "2026-10-15T08:00:00"))))
                .andExpect(status().isBadRequest());
    }
}


