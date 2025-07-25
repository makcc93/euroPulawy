package pl.eurokawa.product;

import com.vaadin.flow.router.NotFoundException;
import org.springframework.stereotype.Service;

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
    public Product create(String name) {
        Product product = new Product();
        product.setName(name);
        return productRepository.save(product);
    }

    @Override
    public Product save(Product product) {
        return productRepository.save(product);
    }

    @Override
    public void delete(Product product) {
        productRepository.delete(product);
    }

    @Override
    public Product findById(Integer id) {
        return productRepository.findById(id).orElseThrow(() -> new NotFoundException("Product with this id not exist!"));
    }
}
