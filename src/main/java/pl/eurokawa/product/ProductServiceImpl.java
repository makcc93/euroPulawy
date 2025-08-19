package pl.eurokawa.product;

import com.vaadin.flow.router.NotFoundException;
import org.apache.commons.validator.Arg;
import org.springframework.stereotype.Service;
import pl.eurokawa.exception.ArgumentNullChecker;
import pl.eurokawa.product.DTO.ProductNameDTO;

import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService{
    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<Product> getAllProducts() {
        return Optional.ofNullable(productRepository.findAllProducts())
                .orElseThrow(() -> new NotFoundException("Cannot find any products!"));
    }

    @Override
    public ProductNameDTO create(String productName) {
        ArgumentNullChecker.check(productName,"Product name");

        Product product = new Product();
        product.setName(productName);

        Product savedProduct = productRepository.save(product);

        return ProductNameDTO.from(savedProduct);
    }

    @Override
    public ProductNameDTO save(Product product) {
        ArgumentNullChecker.check(product,"Product");
        Product savedProduct = productRepository.save(product);

        return ProductNameDTO.from(savedProduct);
    }

    @Override
    public void delete(Integer id) {
        ArgumentNullChecker.check(id,"Id");
        Product product = findById(id);

        productRepository.delete(product);
    }

    @Override
    public Product findById(Integer id) {
        ArgumentNullChecker.check(id,"Id");

        return productRepository.findById(id).orElseThrow(() -> new NotFoundException("Product with this id does not exist!"));
    }
}
