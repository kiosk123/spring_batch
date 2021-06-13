package batch.config.classes;

import java.time.LocalDate;
import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {
    Collection<User> findAllByUpdatedDate(LocalDate updateDate);

    @Query(value = "select max(u.id) from User u")
    long findMaxId();

    @Query(value = "select min(u.id) from User u")
	long findMinId();
}
