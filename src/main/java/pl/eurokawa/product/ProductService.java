package pl.eurokawa.product;

import pl.eurokawa.product.DTO.ProductNameDTO;

import java.util.List;

public interface ProductService {
    List<Product> getAllProducts();
    ProductNameDTO create(String productName);
    ProductNameDTO save(Product product);
    void delete(Integer id);
    Product findById(Integer id);

}
