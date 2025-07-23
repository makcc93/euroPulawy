package pl.eurokawa.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product,Integer>, JpaSpecificationExecutor<Product> {

    @Query(value = "SELECT p.id, p.name FROM product p ORDER BY p.id ASC",nativeQuery = true)
    List<Product> findAllProducts();
}
