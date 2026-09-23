package co.fcv.citas.identity;
import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
interface UserRepository extends JpaRepository<UserEntity,Long>{ boolean existsByEmailIgnoreCase(String email); boolean existsByDocumentTypeAndDocumentNumber(String type,String number); Optional<UserEntity> findByEmailIgnoreCase(String email); }
