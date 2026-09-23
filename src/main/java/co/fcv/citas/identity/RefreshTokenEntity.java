package co.fcv.citas.identity;
import jakarta.persistence.*; import java.time.Instant;
@Entity @Table(name="refresh_tokens") class RefreshTokenEntity { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id; @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id") UserEntity user; @Column(name="token_hash", length=64) String tokenHash; @Column(name="expires_at") Instant expiresAt; protected RefreshTokenEntity(){} RefreshTokenEntity(UserEntity u,String h,Instant e){user=u;tokenHash=h;expiresAt=e;} }
