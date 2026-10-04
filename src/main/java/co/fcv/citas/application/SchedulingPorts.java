package co.fcv.citas.application;

import co.fcv.citas.domain.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public final class SchedulingPorts {
    private SchedulingPorts() {}

    public interface Catalogs {
        List<Location> findAllLocations();
        Optional<Location> findLocationById(Short id);
        List<Specialty> findAllSpecialties();
        Optional<Specialty> findSpecialtyById(Short id);
        List<AppointmentStatus> findAllAppointmentStatuses();
        Optional<AppointmentStatus> findStatusById(Short id);
        Optional<AppointmentStatus> findStatusByCode(String code);
    }

    public interface Professionals {
        List<Professional> findAll(Boolean active, Short specialtyId, Short locationId);
        Optional<Professional> findById(Long id);
        Optional<Professional> findByUserId(Long userId);
        Professional create(Long userId, String professionalCode, String licenseNumber, List<Short> specialtyIds, List<Short> locationIds);
        Professional save(Professional professional);
    }

    public interface AvailabilityBlocks {
        List<AvailabilityBlock> findByProfessional(Long professionalId);
        List<AvailabilityBlock> findByProfessionalAndDate(Long professionalId, LocalDate date);
        Optional<AvailabilityBlock> findById(Long id);
        AvailabilityBlock save(AvailabilityBlock block);
        void delete(Long id);
    }

    public interface ProfessionalSlots {
        List<ProfessionalSlot> findByBlockId(Long blockId);
        List<ProfessionalSlot> findAvailableSlots(Short locationId, Long professionalId, LocalDate date);
        List<ProfessionalSlot> findSlotsForRange(Long professionalId, LocalDateTime startAt, LocalDateTime endAt);
        List<ProfessionalSlot> findSlotsByAppointmentId(Long appointmentId);
        void saveAll(List<ProfessionalSlot> slots);
        int assignSlots(List<Long> slotIds, Long appointmentId);
        void releaseSlots(Long appointmentId);
    }

    public interface Appointments {
        Appointment save(Appointment appointment);
        Optional<Appointment> findById(Long id);
        List<Appointment> findByPatientUserId(Long patientUserId);
        List<Appointment> findByProfessionalId(Long professionalId, LocalDate date, Short statusId);
        List<Appointment> findByFilters(Short statusId, Short locationId, Long professionalId, LocalDate date);
    }

    public interface AppointmentHistories {
        void record(AppointmentStatusHistory history);
        List<AppointmentStatusHistory> findByAppointmentId(Long appointmentId);
    }

    public interface RescheduleRequests {
        RescheduleRequest save(RescheduleRequest request);
        Optional<RescheduleRequest> findById(Long id);
        List<RescheduleRequest> findByUserId(Long userId);
        List<RescheduleRequest> findPending();
    }

    public interface InsuranceManagement {
        List<Insurance.Regime> findRegimes();
        List<Insurance.EpsEntity> findEps(Boolean activeOnly);
        Insurance.EpsEntity saveEps(Insurance.EpsEntity eps);
        Optional<Insurance.EpsEntity> findEpsById(Long id);
        List<Insurance.Plan> findPlans(Long epsId, Boolean activeOnly);
        Insurance.Plan savePlan(Insurance.Plan plan);
        Optional<Insurance.Plan> findPlanById(Long id);
        Optional<Insurance.UserAffiliation> findUserAffiliation(Long userId);
        Insurance.UserAffiliation saveAffiliation(Insurance.UserAffiliation affiliation);
    }

    public interface PasswordResets {
        PasswordResetToken save(PasswordResetToken token);
        Optional<PasswordResetToken> findByTokenHash(String tokenHash);
        void markUsed(Long tokenId);
    }
}
