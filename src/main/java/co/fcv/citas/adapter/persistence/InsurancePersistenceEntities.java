package co.fcv.citas.adapter.persistence;

import jakarta.persistence.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// 1. Régimen
@Entity
@Table(name = "insurance_regimes")
class InsuranceRegimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    public InsuranceRegimeEntity() {}
    public InsuranceRegimeEntity(Short id, String code, String name) {
        this.id = id; this.code = code; this.name = name;
    }

    public Short getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
}

interface InsuranceRegimeJpaRepository extends JpaRepository<InsuranceRegimeEntity, Short> {
    Optional<InsuranceRegimeEntity> findByCode(String code);
}

// 2. EPS
@Entity
@Table(name = "eps")
class EpsJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false)
    private boolean active = true;

    public EpsJpaEntity() {}
    public EpsJpaEntity(Long id, String code, String name, boolean active) {
        this.id = id; this.code = code; this.name = name; this.active = active;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public boolean isActive() { return active; }

    public void setName(String name) { this.name = name; }
    public void setActive(boolean active) { this.active = active; }
}

interface EpsJpaRepository extends JpaRepository<EpsJpaEntity, Long> {
    Optional<EpsJpaEntity> findByCode(String code);
    List<EpsJpaEntity> findByActiveTrue();
}

// 3. Plan EPS
@Entity
@Table(name = "eps_plans")
class EpsPlanJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eps_id", nullable = false)
    private EpsJpaEntity eps;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regime_id", nullable = false)
    private InsuranceRegimeEntity regime;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false)
    private boolean active = true;

    public EpsPlanJpaEntity() {}
    public EpsPlanJpaEntity(Long id, EpsJpaEntity eps, InsuranceRegimeEntity regime, String code, String name, boolean active) {
        this.id = id; this.eps = eps; this.regime = regime; this.code = code; this.name = name; this.active = active;
    }

    public Long getId() { return id; }
    public EpsJpaEntity getEps() { return eps; }
    public InsuranceRegimeEntity getRegime() { return regime; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public boolean isActive() { return active; }

    public void setName(String name) { this.name = name; }
    public void setActive(boolean active) { this.active = active; }
}

interface EpsPlanJpaRepository extends JpaRepository<EpsPlanJpaEntity, Long> {
    List<EpsPlanJpaEntity> findByEpsId(Long epsId);
    List<EpsPlanJpaEntity> findByActiveTrue();
}

// 4. Afiliación de Usuario
@Entity
@Table(name = "user_insurance_affiliations")
class UserInsuranceAffiliationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private EpsPlanJpaEntity plan;

    @Column(name = "membership_number", nullable = false, length = 50)
    private String membershipNumber;

    @Column(name = "is_current", nullable = false)
    private boolean current = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public UserInsuranceAffiliationEntity() {}
    public UserInsuranceAffiliationEntity(Long id, UserEntity user, EpsPlanJpaEntity plan, String membershipNumber, boolean current, LocalDateTime createdAt) {
        this.id = id; this.user = user; this.plan = plan; this.membershipNumber = membershipNumber; this.current = current;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public UserEntity getUser() { return user; }
    public EpsPlanJpaEntity getPlan() { return plan; }
    public String getMembershipNumber() { return membershipNumber; }
    public boolean isCurrent() { return current; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCurrent(boolean current) { this.current = current; }
}

interface UserInsuranceAffiliationJpaRepository extends JpaRepository<UserInsuranceAffiliationEntity, Long> {
    List<UserInsuranceAffiliationEntity> findByUserId(Long userId);
    Optional<UserInsuranceAffiliationEntity> findByUserIdAndCurrentTrue(Long userId);
}
