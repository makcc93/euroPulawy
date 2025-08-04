package pl.eurokawa.terms;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TermsOfServiceRepository extends JpaRepository<TermsOfService,Integer>, JpaSpecificationExecutor<TermsOfService> {

    @Query("SELECT t FROM TermsOfService t ORDER BY t.createdAt DESC")
    List<TermsOfService> termsOfServiceList();

    @Query(value = "SELECT * FROM terms_of_service t WHERE t.is_actual = 1 ORDER BY t.created_at DESC LIMIT 1",nativeQuery = true)
    TermsOfService findCurrentActual();

}
