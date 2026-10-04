package co.fcv.citas.adapter.persistence;

import co.fcv.citas.application.SchedulingPorts.InsuranceManagement;
import co.fcv.citas.domain.Insurance;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
public class InsurancePersistenceAdapter implements InsuranceManagement {
    private final InsuranceRegimeJpaRepository regimeRepo;
    private final EpsJpaRepository epsRepo;
    private final EpsPlanJpaRepository planRepo;
    private final UserInsuranceAffiliationJpaRepository affiliationRepo;
    private final UserJpaRepository userRepo;

    public InsurancePersistenceAdapter(InsuranceRegimeJpaRepository regimeRepo,
                                       EpsJpaRepository epsRepo,
                                       EpsPlanJpaRepository planRepo,
                                       UserInsuranceAffiliationJpaRepository affiliationRepo,
                                       UserJpaRepository userRepo) {
        this.regimeRepo = regimeRepo;
        this.epsRepo = epsRepo;
        this.planRepo = planRepo;
        this.affiliationRepo = affiliationRepo;
        this.userRepo = userRepo;
    }

    @Override
    public List<Insurance.Regime> findRegimes() {
        return regimeRepo.findAll().stream()
                .map(r -> new Insurance.Regime(r.getId(), r.getCode(), r.getName()))
                .toList();
    }

    @Override
    public List<Insurance.EpsEntity> findEps(Boolean activeOnly) {
        var list = (activeOnly != null && activeOnly) ? epsRepo.findByActiveTrue() : epsRepo.findAll();
        return list.stream()
                .map(e -> new Insurance.EpsEntity(e.getId(), e.getCode(), e.getName(), e.isActive()))
                .toList();
    }

    @Override
    public Insurance.EpsEntity saveEps(Insurance.EpsEntity eps) {
        EpsJpaEntity entity = eps.id() != null ? epsRepo.findById(eps.id()).orElse(new EpsJpaEntity()) : new EpsJpaEntity();
        entity = new EpsJpaEntity(eps.id(), eps.code(), eps.name(), eps.active());
        var saved = epsRepo.save(entity);
        return new Insurance.EpsEntity(saved.getId(), saved.getCode(), saved.getName(), saved.isActive());
    }

    @Override
    public Optional<Insurance.EpsEntity> findEpsById(Long id) {
        return epsRepo.findById(id).map(e -> new Insurance.EpsEntity(e.getId(), e.getCode(), e.getName(), e.isActive()));
    }

    @Override
    public List<Insurance.Plan> findPlans(Long epsId, Boolean activeOnly) {
        List<EpsPlanJpaEntity> list;
        if (epsId != null) {
            list = planRepo.findByEpsId(epsId);
        } else if (activeOnly != null && activeOnly) {
            list = planRepo.findByActiveTrue();
        } else {
            list = planRepo.findAll();
        }
        return list.stream()
                .map(p -> new Insurance.Plan(p.getId(), p.getEps().getId(), p.getRegime().getId(), p.getCode(), p.getName(), p.isActive()))
                .toList();
    }

    @Override
    public Insurance.Plan savePlan(Insurance.Plan plan) {
        var eps = epsRepo.findById(plan.epsId())
                .orElseThrow(() -> new IllegalArgumentException("EPS no encontrada"));
        var regime = regimeRepo.findById(plan.regimeId())
                .orElseThrow(() -> new IllegalArgumentException("Régimen no encontrado"));
        var entity = new EpsPlanJpaEntity(plan.id(), eps, regime, plan.code(), plan.name(), plan.active());
        var saved = planRepo.save(entity);
        return new Insurance.Plan(saved.getId(), saved.getEps().getId(), saved.getRegime().getId(), saved.getCode(), saved.getName(), saved.isActive());
    }

    @Override
    public Optional<Insurance.Plan> findPlanById(Long id) {
        return planRepo.findById(id).map(p -> new Insurance.Plan(p.getId(), p.getEps().getId(), p.getRegime().getId(), p.getCode(), p.getName(), p.isActive()));
    }

    @Override
    public Optional<Insurance.UserAffiliation> findUserAffiliation(Long userId) {
        return affiliationRepo.findByUserIdAndCurrentTrue(userId)
                .map(a -> new Insurance.UserAffiliation(a.getId(), a.getUser().getId(), a.getPlan().getId(), a.getMembershipNumber(), a.isCurrent(), a.getCreatedAt()));
    }

    @Override
    public Insurance.UserAffiliation saveAffiliation(Insurance.UserAffiliation affiliation) {
        var user = userRepo.findById(affiliation.userId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        var plan = planRepo.findById(affiliation.planId())
                .orElseThrow(() -> new IllegalArgumentException("Plan no encontrado"));

        // Desactivar afiliaciones anteriores si es la actual
        if (affiliation.isCurrent()) {
            var existing = affiliationRepo.findByUserId(affiliation.userId());
            for (var prev : existing) {
                prev.setCurrent(false);
                affiliationRepo.save(prev);
            }
        }

        var entity = new UserInsuranceAffiliationEntity(affiliation.id(), user, plan, affiliation.membershipNumber(), affiliation.isCurrent(), affiliation.createdAt());
        var saved = affiliationRepo.save(entity);
        return new Insurance.UserAffiliation(saved.getId(), saved.getUser().getId(), saved.getPlan().getId(), saved.getMembershipNumber(), saved.isCurrent(), saved.getCreatedAt());
    }
}
