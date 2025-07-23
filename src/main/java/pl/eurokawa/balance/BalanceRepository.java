package pl.eurokawa.balance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface BalanceRepository extends JpaRepository<Balance,Integer>, JpaSpecificationExecutor<Balance> {

    @Query(value = "SELECT * FROM balance ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Balance findLastBalanceValue();

    @Query("SELECT b FROM Balance b WHERE b.user.id = :userId ORDER BY b.id DESC")
    Optional<Balance> findUserLastBalanceOperation(@Param("userId") Integer userId);
}
