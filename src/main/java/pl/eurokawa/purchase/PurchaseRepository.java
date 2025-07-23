package pl.eurokawa.purchase;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Integer>, JpaSpecificationExecutor<Purchase> {

    @Query("SELECT p FROM Purchase p WHERE p.isConfirmed = true ORDER BY p.id DESC")
    List<Purchase> findAllConfirmedPurchases();

    @Query("SELECT p FROM Purchase p WHERE p.isConfirmed AND p.user.id = :userId ORDER BY p.id DESC")
    List<Purchase> findUserConfirmedPurchases(@Param("userId") Integer userId);

    @Query("SELECT p FROM Purchase p JOIN p.user u WHERE p.isSaved = true AND p.isConfirmed = false ORDER BY p.id")
    List<Purchase> findAllSavedNotConfirmedPurchases();

    @Query("SELECT p FROM Purchase p JOIN p.user u WHERE p.isSaved = true AND p.isConfirmed = false AND u.id = :id ORDER BY p.id DESC")
    List<Purchase> findUserSavedPurchases(@Param("id") Integer id);

    @Query("SELECT p FROM Purchase p WHERE p.receiptImagePath = :fileName")
    Optional<Purchase> findPurchaseByImageName(String fileName);

    @Query("DELETE FROM Purchase p WHERE p.id = :id")
    void deletePurchaseById(@Param("id") Integer id);
}
