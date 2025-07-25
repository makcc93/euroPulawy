package pl.eurokawa.product;

import org.springframework.stereotype.Service;

import java.util.List;

public interface ProductService {
    List<Product> getAllProducts();
    Product create(String name);
    Product save(Product product);
    void delete(Product product);

}
