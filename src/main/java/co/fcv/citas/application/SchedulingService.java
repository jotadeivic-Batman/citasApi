package co.fcv.citas.application;

import co.fcv.citas.domain.*;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static co.fcv.citas.application.SchedulingPorts.*;

public class SchedulingService {
    private final Catalogs catalogs;
    private final Professionals professionals;
    private final AvailabilityBlocks availabilityBlocks;
    private final ProfessionalSlots professionalSlots;
    private final Appointments appointments;
    private final AppointmentHistories histories;
    private final RescheduleRequests rescheduleRequests;
    private final InsuranceManagement insuranceManagement;
    private final PasswordResets passwordResets;
    private final AuthPorts.Users users;
    private final AuthPorts.Passwords passwords;
    private final Clock clock;

    public SchedulingService(Catalogs catalogs, Professionals professionals,
                             AvailabilityBlocks availabilityBlocks,
                             ProfessionalSlots professionalSlots,
                             Appointments appointments,
                             AppointmentHistories histories,
                             RescheduleRequests rescheduleRequests,
                             InsuranceManagement insuranceManagement,
                             PasswordResets passwordResets,
                             AuthPorts.Users users,
                             AuthPorts.Passwords passwords,
                             Clock clock) {
        this.catalogs = catalogs;
        this.professionals = professionals;
        this.availabilityBlocks = availabilityBlocks;
        this.professionalSlots = professionalSlots;
        this.appointments = appointments;
        this.histories = histories;
        this.rescheduleRequests = rescheduleRequests;
        this.insuranceManagement = insuranceManagement;
        this.passwordResets = passwordResets;
        this.users = users;
        this.passwords = passwords;
        this.clock = clock;
    }

    // --- Catálogos ---
    public List<Location> getLocations() { return catalogs.findAllLocations(); }
    public List<Specialty> getSpecialties() { return catalogs.findAllSpecialties(); }
    public List<AppointmentStatus> getAppointmentStatuses() { return catalogs.findAllAppointmentStatuses(); }

    // --- Profesionales ---
    public record CreateProfessionalCommand(
            String firstName, String lastName, String documentType, String documentNumber,
            String email, String phone, String password,
            String professionalCode, String licenseNumber,
            List<Short> specialtyIds, List<Short> locationIds
    ) {}

    public Professional createProfessional(CreateProfessionalCommand cmd) {
        String hash = passwords.hash(cmd.password());
        User user = users.create(new User(
                null, cmd.firstName().strip(), cmd.lastName().strip(),
                cmd.documentType(), cmd.documentNumber().strip(),
                cmd.email().strip().toLowerCase(Locale.ROOT),
                cmd.phone().strip(), hash, true, Set.of("USER", "PROFESSIONAL")
        ));
        return professionals.create(user.id(), cmd.professionalCode().strip(), cmd.licenseNumber().strip(), cmd.specialtyIds(), cmd.locationIds());
    }

    public List<Professional> listProfessionals(Boolean active, Short specialtyId, Short locationId) {
        return professionals.findAll(active, specialtyId, locationId);
    }

    public Professional getProfessional(Long id) {
        return professionals.findById(id)
                .orElseThrow(() -> SchedulingFailure.notFound("Profesional no encontrado."));
    }

    public Professional getProfessionalByUserId(Long userId) {
        return professionals.findByUserId(userId)
                .orElseThrow(() -> SchedulingFailure.notFound("Perfil profesional no encontrado para este usuario."));
    }

    // --- Bloques de Disponibilidad ---
    public record CreateBlockCommand(Long professionalId, Short locationId,
                                    LocalDate date, LocalTime startTime, LocalTime endTime) {}

    public AvailabilityBlock createBlock(CreateBlockCommand cmd) {
        LocalDate today = LocalDate.now(clock);
        if (cmd.date().isBefore(today)) {
            throw SchedulingFailure.invalid("No se pueden crear bloques de disponibilidad en el pasado.");
        }
        if (!cmd.startTime().isBefore(cmd.endTime())) {
            throw SchedulingFailure.invalid("La hora de inicio debe ser anterior a la hora de fin.");
        }

        Professional prof = getProfessional(cmd.professionalId());
        boolean hasLocation = prof.locations().stream().anyMatch(l -> l.id().equals(cmd.locationId()));
        if (!hasLocation) {
            throw SchedulingFailure.invalid("El profesional no tiene asignada la sede especificada.");
        }

        List<AvailabilityBlock> existing = availabilityBlocks.findByProfessionalAndDate(cmd.professionalId(), cmd.date());
        for (AvailabilityBlock b : existing) {
            if (b.active() && !(cmd.endTime().compareTo(b.startTime()) <= 0 || cmd.startTime().compareTo(b.endTime()) >= 0)) {
                throw SchedulingFailure.conflict("Ya existe un bloque en ese horario para este profesional.");
            }
        }

        AvailabilityBlock savedBlock = availabilityBlocks.save(
                new AvailabilityBlock(null, cmd.professionalId(), cmd.locationId(), cmd.date(), cmd.startTime(), cmd.endTime(), true));

        // Discretizar en slots de 30 minutos
        List<ProfessionalSlot> slots = new ArrayList<>();
        LocalTime current = cmd.startTime();
        while (current.plusMinutes(30).compareTo(cmd.endTime()) <= 0) {
            LocalDateTime startAt = LocalDateTime.of(cmd.date(), current);
            LocalDateTime endAt = LocalDateTime.of(cmd.date(), current.plusMinutes(30));
            slots.add(new ProfessionalSlot(null, savedBlock.id(), cmd.professionalId(), null, startAt, endAt));
            current = current.plusMinutes(30);
        }
        professionalSlots.saveAll(slots);
        return savedBlock;
    }

    public void deleteBlock(Long blockId, Long requestingUserId, boolean isAdmin) {
        AvailabilityBlock block = availabilityBlocks.findById(blockId)
                .orElseThrow(() -> SchedulingFailure.notFound("Bloque no encontrado."));
        if (!isAdmin) {
            Professional prof = getProfessionalByUserId(requestingUserId);
            if (!prof.id().equals(block.professionalId())) {
                throw SchedulingFailure.forbidden("No tienes permiso para eliminar este bloque.");
            }
        }
        List<ProfessionalSlot> slots = professionalSlots.findByBlockId(blockId);
        boolean hasAppointments = slots.stream().anyMatch(s -> s.appointmentId() != null);
        if (hasAppointments) {
            throw SchedulingFailure.conflict("No se puede eliminar un bloque con citas programadas.");
        }
        availabilityBlocks.delete(blockId);
    }

    public List<AvailabilityBlock> listBlocks(Long professionalId) {
        return availabilityBlocks.findByProfessional(professionalId);
    }

    // --- Consulta de Horarios Disponibles ---
    public record AvailableSlotDto(LocalDateTime startAt, LocalDateTime endAt,
                                  Long professionalId, Short locationId, Short specialtyId) {}

    public List<AvailableSlotDto> searchAvailableSlots(Short locationId, Short specialtyId,
                                                      Long professionalId, LocalDate date) {
        Specialty specialty = catalogs.findSpecialtyById(specialtyId)
                .orElseThrow(() -> SchedulingFailure.notFound("Especialidad no válida."));

        List<ProfessionalSlot> freeSlots = professionalSlots.findAvailableSlots(locationId, professionalId, date);
        LocalDateTime now = LocalDateTime.now(clock);

        int durationMinutes = specialty.durationMinutes();
        List<AvailableSlotDto> results = new ArrayList<>();

        if (durationMinutes == 30) {
            for (ProfessionalSlot slot : freeSlots) {
                if (slot.startAt().isAfter(now)) {
                    Long profId = slot.professionalId() != null ? slot.professionalId() : professionalId;
                    results.add(new AvailableSlotDto(slot.startAt(), slot.endAt(), profId, locationId, specialtyId));
                }
            }
        } else if (durationMinutes == 60) {
            // Requiere dos slots consecutivos en el mismo bloque
            Map<Long, List<ProfessionalSlot>> byBlock = new HashMap<>();
            for (ProfessionalSlot slot : freeSlots) {
                byBlock.computeIfAbsent(slot.availabilityBlockId(), k -> new ArrayList<>()).add(slot);
            }
            for (List<ProfessionalSlot> blockSlots : byBlock.values()) {
                blockSlots.sort(Comparator.comparing(ProfessionalSlot::startAt));
                for (int i = 0; i < blockSlots.size() - 1; i++) {
                    ProfessionalSlot a = blockSlots.get(i);
                    ProfessionalSlot b = blockSlots.get(i + 1);
                    if (a.endAt().equals(b.startAt()) && a.startAt().isAfter(now)) {
                        Long profId = a.professionalId() != null ? a.professionalId() : professionalId;
                        results.add(new AvailableSlotDto(a.startAt(), b.endAt(), profId, locationId, specialtyId));
                    }
                }
            }
        }
        return results;
    }

    // --- Citas (Appointments) ---
    public record BookAppointmentCommand(Long patientUserId, Long professionalId,
                                         Short locationId, Short specialtyId,
                                         LocalDateTime startAt) {}

    public Appointment bookAppointment(BookAppointmentCommand cmd) {
        LocalDateTime now = LocalDateTime.now(clock);
        if (cmd.startAt().isBefore(now)) {
            throw SchedulingFailure.invalid("No se puede agendar una cita en el pasado.");
        }

        Specialty specialty = catalogs.findSpecialtyById(cmd.specialtyId())
                .orElseThrow(() -> SchedulingFailure.notFound("Especialidad no encontrada."));
        if (!specialty.active()) {
            throw SchedulingFailure.invalid("La especialidad seleccionada no está activa.");
        }

        Professional prof = getProfessional(cmd.professionalId());
        if (!prof.active()) {
            throw SchedulingFailure.invalid("El profesional no se encuentra activo.");
        }
        boolean hasSpecialty = prof.specialties().stream().anyMatch(s -> s.id().equals(cmd.specialtyId()));
        if (!hasSpecialty) {
            throw SchedulingFailure.invalid("El profesional no tiene asignada la especialidad seleccionada.");
        }
        boolean hasLocation = prof.locations().stream().anyMatch(l -> l.id().equals(cmd.locationId()));
        if (!hasLocation) {
            throw SchedulingFailure.invalid("El profesional no atiende en la sede seleccionada.");
        }

        LocalDateTime endAt = cmd.startAt().plusMinutes(specialty.durationMinutes());
        List<ProfessionalSlot> requiredSlots = professionalSlots.findSlotsForRange(cmd.professionalId(), cmd.startAt(), endAt);

        int expectedSlotsCount = specialty.durationMinutes() / 30;
        if (requiredSlots.size() != expectedSlotsCount) {
            throw SchedulingFailure.conflict("El horario seleccionado no cuenta con slots disponibles continuos.");
        }
        for (ProfessionalSlot s : requiredSlots) {
            if (!s.isAvailable()) {
                throw SchedulingFailure.conflict("Uno o más slots del horario seleccionado ya se encuentran ocupados.");
            }
        }

        // RN-02 y RN-03: Cita general se aprueba automáticamente; especializada queda en REQUESTED
        String statusCode = (specialty.isGeneral() || !specialty.requiresAdminApproval())
                ? AppointmentStatus.APPROVED
                : AppointmentStatus.REQUESTED;

        AppointmentStatus status = catalogs.findStatusByCode(statusCode)
                .orElseThrow(() -> SchedulingFailure.invalid("Estado no configurado: " + statusCode));

        Appointment appointment = appointments.save(new Appointment(
                null, cmd.patientUserId(), cmd.professionalId(), cmd.locationId(), cmd.specialtyId(),
                status.id(), cmd.startAt(), endAt, null, null, null));

        // Retener/asignar los slots a la cita creada
        List<Long> slotIds = requiredSlots.stream().map(ProfessionalSlot::id).toList();
        int assignedSlots = professionalSlots.assignSlots(slotIds, appointment.id());
        if (assignedSlots != slotIds.size()) {
            throw SchedulingFailure.conflict("Uno o más slots ya fueron reservados por otra solicitud.");
        }

        // Registrar auditoría de estado inicial
        histories.record(new AppointmentStatusHistory(
                null, appointment.id(), status.id(), cmd.patientUserId(), "USER", null,
                statusCode.equals(AppointmentStatus.APPROVED) ? "Cita general auto-aprobada" : "Solicitud de cita especializada"));

        return appointment;
    }

    public Appointment cancelAppointment(Long appointmentId, Long userId, boolean isAdmin) {
        Appointment app = appointments.findById(appointmentId)
                .orElseThrow(() -> SchedulingFailure.notFound("Cita no encontrada."));

        if (!isAdmin && !app.patientUserId().equals(userId)) {
            throw SchedulingFailure.forbidden("No tienes permiso para cancelar esta cita.");
        }

        AppointmentStatus currentStatus = catalogs.findStatusById(app.statusId())
                .orElseThrow(() -> SchedulingFailure.invalid("Estado actual inválido."));
        if (currentStatus.isTerminal()) {
            throw SchedulingFailure.invalid("No se puede cancelar una cita que ya finalizó o fue cancelada.");
        }

        LocalDateTime now = LocalDateTime.now(clock);
        if (app.scheduledStartAt().isBefore(now)) {
            throw SchedulingFailure.invalid("No se puede cancelar una cita en el pasado.");
        }

        AppointmentStatus cancelled = catalogs.findStatusByCode(AppointmentStatus.CANCELLED)
                .orElseThrow(() -> SchedulingFailure.invalid("Estado CANCELLED no configurado."));

        Appointment updated = appointments.save(new Appointment(
                app.id(), app.patientUserId(), app.professionalId(), app.locationId(), app.specialtyId(),
                cancelled.id(), app.scheduledStartAt(), app.scheduledEndAt(), app.rejectionReason(),
                app.createdAt(), null));

        // Liberar slots reservados
        professionalSlots.releaseSlots(appointmentId);

        histories.record(new AppointmentStatusHistory(
                null, app.id(), cancelled.id(), userId, isAdmin ? "ADMIN" : "USER", null,
                isAdmin ? "Cancelada por administración" : "Cancelada por el paciente"));

        return updated;
    }

    public Appointment approveAppointment(Long appointmentId, Long adminUserId) {
        Appointment app = appointments.findById(appointmentId)
                .orElseThrow(() -> SchedulingFailure.notFound("Cita no encontrada."));

        AppointmentStatus currentStatus = catalogs.findStatusById(app.statusId())
                .orElseThrow(() -> SchedulingFailure.invalid("Estado actual inválido."));
        if (!AppointmentStatus.REQUESTED.equals(currentStatus.code())) {
            throw SchedulingFailure.invalid("Solo se pueden aprobar citas en estado REQUESTED.");
        }

        AppointmentStatus approved = catalogs.findStatusByCode(AppointmentStatus.APPROVED)
                .orElseThrow(() -> SchedulingFailure.invalid("Estado APPROVED no configurado."));

        Appointment updated = appointments.save(new Appointment(
                app.id(), app.patientUserId(), app.professionalId(), app.locationId(), app.specialtyId(),
                approved.id(), app.scheduledStartAt(), app.scheduledEndAt(), null,
                app.createdAt(), null));

        histories.record(new AppointmentStatusHistory(
                null, app.id(), approved.id(), adminUserId, "ADMIN", null, "Aprobada por administración"));

        return updated;
    }

    public Appointment rejectAppointment(Long appointmentId, Long adminUserId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw SchedulingFailure.invalid("El motivo de rechazo es obligatorio (RN-04).");
        }

        Appointment app = appointments.findById(appointmentId)
                .orElseThrow(() -> SchedulingFailure.notFound("Cita no encontrada."));

        AppointmentStatus currentStatus = catalogs.findStatusById(app.statusId())
                .orElseThrow(() -> SchedulingFailure.invalid("Estado actual inválido."));
        if (!AppointmentStatus.REQUESTED.equals(currentStatus.code())) {
            throw SchedulingFailure.invalid("Solo se pueden rechazar citas en estado REQUESTED.");
        }

        AppointmentStatus rejected = catalogs.findStatusByCode(AppointmentStatus.REJECTED)
                .orElseThrow(() -> SchedulingFailure.invalid("Estado REJECTED no configurado."));

        Appointment updated = appointments.save(new Appointment(
                app.id(), app.patientUserId(), app.professionalId(), app.locationId(), app.specialtyId(),
                rejected.id(), app.scheduledStartAt(), app.scheduledEndAt(), reason.strip(),
                app.createdAt(), null));

        // Liberar slots
        professionalSlots.releaseSlots(appointmentId);

        histories.record(new AppointmentStatusHistory(
                null, app.id(), rejected.id(), adminUserId, "ADMIN", null, reason.strip()));

        return updated;
    }

    public Appointment completeAppointment(Long appointmentId, Long professionalUserId) {
        Appointment app = appointments.findById(appointmentId)
                .orElseThrow(() -> SchedulingFailure.notFound("Cita no encontrada."));

        Professional prof = getProfessionalByUserId(professionalUserId);
        if (!app.professionalId().equals(prof.id())) {
            throw SchedulingFailure.forbidden("Solo el profesional asignado puede marcar la cita como completada.");
        }

        AppointmentStatus currentStatus = catalogs.findStatusById(app.statusId())
                .orElseThrow(() -> SchedulingFailure.invalid("Estado actual inválido."));
        if (!AppointmentStatus.APPROVED.equals(currentStatus.code())) {
            throw SchedulingFailure.invalid("Solo se pueden completar citas previamente aprobadas.");
        }

        AppointmentStatus completed = catalogs.findStatusByCode(AppointmentStatus.COMPLETED)
                .orElseThrow(() -> SchedulingFailure.invalid("Estado COMPLETED no configurado."));

        Appointment updated = appointments.save(new Appointment(
                app.id(), app.patientUserId(), app.professionalId(), app.locationId(), app.specialtyId(),
                completed.id(), app.scheduledStartAt(), app.scheduledEndAt(), app.rejectionReason(),
                app.createdAt(), null));

        histories.record(new AppointmentStatusHistory(
                null, app.id(), completed.id(), professionalUserId, "PROFESSIONAL", null, "Atención completada"));

        return updated;
    }

    public Appointment noShowAppointment(Long appointmentId, Long professionalUserId, String reason) {
        Appointment app = appointments.findById(appointmentId)
                .orElseThrow(() -> SchedulingFailure.notFound("Cita no encontrada."));

        Professional prof = getProfessionalByUserId(professionalUserId);
        if (!app.professionalId().equals(prof.id())) {
            throw SchedulingFailure.forbidden("Solo el profesional asignado puede marcar inasistencia.");
        }

        AppointmentStatus currentStatus = catalogs.findStatusById(app.statusId())
                .orElseThrow(() -> SchedulingFailure.invalid("Estado actual inválido."));
        if (!AppointmentStatus.APPROVED.equals(currentStatus.code())) {
            throw SchedulingFailure.invalid("Solo se puede registrar inasistencia en citas aprobadas.");
        }

        AppointmentStatus noShow = catalogs.findStatusByCode(AppointmentStatus.NO_SHOW)
                .orElseThrow(() -> SchedulingFailure.invalid("Estado NO_SHOW no configurado."));

        Appointment updated = appointments.save(new Appointment(
                app.id(), app.patientUserId(), app.professionalId(), app.locationId(), app.specialtyId(),
                noShow.id(), app.scheduledStartAt(), app.scheduledEndAt(), app.rejectionReason(),
                app.createdAt(), null));

        histories.record(new AppointmentStatusHistory(
                null, app.id(), noShow.id(), professionalUserId, "PROFESSIONAL", null,
                reason == null || reason.isBlank() ? "Paciente no se presentó" : reason.strip()));

        return updated;
    }

    public List<Appointment> getMyAppointments(Long patientUserId) {
        return appointments.findByPatientUserId(patientUserId);
    }

    public List<Appointment> getProfessionalAppointments(Long professionalUserId, LocalDate date) {
        Professional prof = getProfessionalByUserId(professionalUserId);
        AppointmentStatus approved = catalogs.findStatusByCode(AppointmentStatus.APPROVED)
                .orElseThrow(() -> SchedulingFailure.invalid("Estado APPROVED no configurado."));
        return appointments.findByProfessionalId(prof.id(), date, approved.id());
    }

    public List<Appointment> getAdminAppointments(Short statusId, Short locationId, Long professionalId, LocalDate date) {
        return appointments.findByFilters(statusId, locationId, professionalId, date);
    }

    public List<AppointmentStatusHistory> getAppointmentHistory(Long appointmentId) {
        return histories.findByAppointmentId(appointmentId);
    }

    // ========================================================
    // --- RF-15: Reprogramación de Citas (S4) ---
    // ========================================================
    public record RequestRescheduleCommand(Long appointmentId, Long patientUserId,
                                           Short requestedLocationId, LocalDateTime requestedStartAt) {}

    public RescheduleRequest requestReschedule(RequestRescheduleCommand cmd) {
        LocalDateTime now = LocalDateTime.now(clock);
        if (cmd.requestedStartAt().isBefore(now)) {
            throw SchedulingFailure.invalid("No se puede reprogramar a una fecha u hora en el pasado.");
        }

        Appointment app = appointments.findById(cmd.appointmentId())
                .orElseThrow(() -> SchedulingFailure.notFound("Cita no encontrada."));

        if (!app.patientUserId().equals(cmd.patientUserId())) {
            throw SchedulingFailure.forbidden("No tienes permiso para solicitar la reprogramación de esta cita.");
        }

        AppointmentStatus currentStatus = catalogs.findStatusById(app.statusId())
                .orElseThrow(() -> SchedulingFailure.invalid("Estado de cita inválido."));
        if (!AppointmentStatus.APPROVED.equals(currentStatus.code())) {
            throw SchedulingFailure.invalid("Solo las citas en estado APPROVED pueden ser reprogramadas (RF-15).");
        }

        if (app.scheduledStartAt().isBefore(now)) {
            throw SchedulingFailure.invalid("No se puede reprogramar una cita pasada.");
        }

        Specialty specialty = catalogs.findSpecialtyById(app.specialtyId())
                .orElseThrow(() -> SchedulingFailure.notFound("Especialidad no encontrada."));

        LocalDateTime requestedEndAt = cmd.requestedStartAt().plusMinutes(specialty.durationMinutes());
        List<ProfessionalSlot> newSlots = professionalSlots.findSlotsForRange(app.professionalId(), cmd.requestedStartAt(), requestedEndAt);

        int expectedSlotsCount = specialty.durationMinutes() / 30;
        if (newSlots.size() != expectedSlotsCount) {
            throw SchedulingFailure.conflict("La nueva franja seleccionada no cuenta con slots disponibles continuos.");
        }
        for (ProfessionalSlot s : newSlots) {
            if (!s.isAvailable()) {
                throw SchedulingFailure.conflict("Uno o más slots del nuevo horario ya están ocupados o reservados.");
            }
        }

        // Crear solicitud en PENDING (status 1)
        RescheduleRequest req = rescheduleRequests.save(new RescheduleRequest(
                null, app.id(), cmd.patientUserId(), cmd.requestedLocationId(), (short) 1,
                cmd.requestedStartAt(), requestedEndAt, null, null, null));

        histories.record(new AppointmentStatusHistory(
                null, app.id(), app.statusId(), cmd.patientUserId(), "USER", null,
                "Solicitud de reprogramación registrada para " + cmd.requestedStartAt()));

        return req;
    }

    public RescheduleRequest approveReschedule(Long rescheduleId, Long adminUserId) {
        RescheduleRequest req = rescheduleRequests.findById(rescheduleId)
                .orElseThrow(() -> SchedulingFailure.notFound("Solicitud de reprogramación no encontrada."));

        if (req.statusId() != 1) { // 1 = PENDING
            throw SchedulingFailure.invalid("Solo se pueden aprobar solicitudes en estado pendiente.");
        }

        Appointment app = appointments.findById(req.appointmentId())
                .orElseThrow(() -> SchedulingFailure.notFound("Cita no encontrada."));

        // Liberar slots antiguos de la cita
        professionalSlots.releaseSlots(app.id());

        // Asignar los nuevos slots
        List<ProfessionalSlot> newSlots = professionalSlots.findSlotsForRange(app.professionalId(), req.requestedStartAt(), req.requestedEndAt());
        List<Long> slotIds = newSlots.stream().map(ProfessionalSlot::id).toList();
        professionalSlots.assignSlots(slotIds, app.id());

        // Actualizar la cita con la nueva fecha y sede
        Appointment updatedApp = appointments.save(new Appointment(
                app.id(), app.patientUserId(), app.professionalId(), req.requestedLocationId(), app.specialtyId(),
                app.statusId(), req.requestedStartAt(), req.requestedEndAt(), null, app.createdAt(), null));

        // Actualizar la solicitud a APPROVED (status 2)
        RescheduleRequest approvedReq = rescheduleRequests.save(new RescheduleRequest(
                req.id(), req.appointmentId(), req.requestedByUserId(), req.requestedLocationId(), (short) 2,
                req.requestedStartAt(), req.requestedEndAt(), null, req.createdAt(), null));

        histories.record(new AppointmentStatusHistory(
                null, app.id(), app.statusId(), adminUserId, "ADMIN", null,
                "Reprogramación aprobada para " + req.requestedStartAt()));

        return approvedReq;
    }

    public RescheduleRequest rejectReschedule(Long rescheduleId, Long adminUserId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw SchedulingFailure.invalid("El motivo de rechazo de reprogramación es obligatorio.");
        }

        RescheduleRequest req = rescheduleRequests.findById(rescheduleId)
                .orElseThrow(() -> SchedulingFailure.notFound("Solicitud de reprogramación no encontrada."));

        if (req.statusId() != 1) {
            throw SchedulingFailure.invalid("Solo se pueden rechazar solicitudes en estado pendiente.");
        }

        // Actualizar la solicitud a REJECTED (status 3) manteniendo la cita original intacta
        RescheduleRequest rejectedReq = rescheduleRequests.save(new RescheduleRequest(
                req.id(), req.appointmentId(), req.requestedByUserId(), req.requestedLocationId(), (short) 3,
                req.requestedStartAt(), req.requestedEndAt(), reason.strip(), req.createdAt(), null));

        histories.record(new AppointmentStatusHistory(
                null, req.appointmentId(), 2, adminUserId, "ADMIN", null,
                "Reprogramación rechazada: " + reason.strip()));

        return rejectedReq;
    }

    public List<RescheduleRequest> getPendingReschedules() {
        return rescheduleRequests.findPending();
    }

    public List<RescheduleRequest> getMyReschedules(Long userId) {
        return rescheduleRequests.findByUserId(userId);
    }

    // ========================================================
    // --- RF-03: Recuperación de Contraseña (S4) ---
    // ========================================================
    public record PasswordResetResponse(String email, String resetToken, LocalDateTime expiresAt) {}

    public PasswordResetResponse requestPasswordReset(String email) {
        var userOpt = users.findByEmail(email.strip().toLowerCase(Locale.ROOT));
        if (userOpt.isEmpty()) {
            // Retornamos sin exponer si existe o no por seguridad, pero en entorno laboratorio devolvemos respuesta controlada
            throw SchedulingFailure.notFound("No existe una cuenta registrada con el correo indicado.");
        }
        var user = userOpt.get();
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now(clock).plusMinutes(30);

        passwordResets.save(new PasswordResetToken(null, user.id(), token, expiresAt, null, null));
        return new PasswordResetResponse(user.email(), token, expiresAt);
    }

    public void resetPassword(String token, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw SchedulingFailure.invalid("La contraseña debe tener al menos 6 caracteres.");
        }

        PasswordResetToken resetToken = passwordResets.findByTokenHash(token)
                .orElseThrow(() -> SchedulingFailure.notFound("Token de recuperación no válido o inexistente."));

        LocalDateTime now = LocalDateTime.now(clock);
        if (!resetToken.isValid(now)) {
            throw SchedulingFailure.invalid("El token de recuperación ha expirado o ya fue utilizado.");
        }

        var user = users.findById(resetToken.userId())
                .orElseThrow(() -> SchedulingFailure.notFound("Usuario no encontrado."));

        String hash = passwords.hash(newPassword);
        users.update(new User(
                user.id(), user.firstName(), user.lastName(), user.documentType(), user.documentNumber(),
                user.email(), user.phone(), hash, user.active(), user.roles()
        ));

        passwordResets.markUsed(resetToken.id());
    }

    // ========================================================
    // --- RF-06 & RF-04: EPS, Planes y Afiliaciones (S4) ---
    // ========================================================
    public List<Insurance.Regime> getInsuranceRegimes() { return insuranceManagement.findRegimes(); }
    public List<Insurance.EpsEntity> getEps(Boolean activeOnly) { return insuranceManagement.findEps(activeOnly); }
    public Insurance.EpsEntity saveEps(Insurance.EpsEntity eps) { return insuranceManagement.saveEps(eps); }
    public List<Insurance.Plan> getPlans(Long epsId, Boolean activeOnly) { return insuranceManagement.findPlans(epsId, activeOnly); }
    public Insurance.Plan savePlan(Insurance.Plan plan) { return insuranceManagement.savePlan(plan); }
    public Optional<Insurance.UserAffiliation> getUserAffiliation(Long userId) { return insuranceManagement.findUserAffiliation(userId); }
    public Insurance.UserAffiliation affiliateUser(Long userId, Long planId, String membershipNumber) {
        return insuranceManagement.saveAffiliation(new Insurance.UserAffiliation(null, userId, planId, membershipNumber.strip(), true, null));
    }
}

