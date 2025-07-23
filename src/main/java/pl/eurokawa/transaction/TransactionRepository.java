package pl.eurokawa.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction,Long>, JpaSpecificationExecutor<Transaction> {

    @Query("SELECT t FROM Transaction t WHERE t.isSaved = TRUE AND t.isConfirmed = FALSE ORDER BY t.id DESC")
    List<Transaction> findAllSavedNotConfirmedTransactions();

    @Query("SELECT t FROM Transaction t WHERE t.isSaved = TRUE AND t.isConfirmed = TRUE ORDER BY t.id DESC")
    List<Transaction>  findAllConfirmedTransactions();

    @Query("SELECT t FROM Transaction t WHERE t.isSaved = TRUE AND t.isConfirmed = TRUE AND t.user.id = :userId ORDER BY t.id")
    List<Transaction> findAllUserConfirmedTransactions(@Param("userId") Integer userId);

    @Query("SELECT COALESCE(SUM(t.amount),0) FROM Transaction t WHERE t.type = 'DEPOSIT' AND t.user.id = :userId")
    BigDecimal getSumOfUserDeposit(@Param("userId") Integer userId);
}
