package co.fcv.citas.identity;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="users") public class UserEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
 @Column(name="first_names") String firstNames; @Column(name="last_names") String lastNames;
 @Column(name="document_type") String documentType; @Column(name="document_number") String documentNumber;
 String email, phone; @Column(name="password_hash") String passwordHash; String role; @Column(name="created_at") Instant createdAt;
 protected UserEntity(){} UserEntity(String fn,String ln,String dt,String dn,String email,String phone,String hash){this.firstNames=fn;this.lastNames=ln;this.documentType=dt;this.documentNumber=dn;this.email=email;this.phone=phone;this.passwordHash=hash;this.role="USER";this.createdAt=Instant.now();}
}
