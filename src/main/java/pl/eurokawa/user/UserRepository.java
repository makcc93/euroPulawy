package pl.eurokawa.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutor<User> {
    Optional<User> findUserByEmail(String email);

    Optional<User> findUserById(Integer id);

    @Query("SELECT u FROM User u WHERE  u.role != 'NOTCONFIRMED'")
    List<User> findOnlyConfirmedUsers();

}
