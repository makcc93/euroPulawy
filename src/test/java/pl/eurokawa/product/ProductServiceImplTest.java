package pl.eurokawa.product;

import com.vaadin.flow.router.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @InjectMocks
    ProductServiceImpl service;

    @Mock
    ProductRepository repository;

    @Test
    void getAllProducts_workingTest(){
        List<Product> products = List.of(
                new Product("Milk"),
                new Product("Coffee"),
                new Product("Sugar")
        );

        when(repository.findAllProducts()).thenReturn(products);

        List<Product> expectedProducts = service.getAllProducts();

        assertEquals(expectedProducts,products);
        assertEquals(3,expectedProducts.size());
    }

    @Test
    void getAllProducts_productsListIsNull(){
        List<Product> products = null;
        when(repository.findAllProducts()).thenReturn(products);

        assertThrows(NotFoundException.class,() -> service.getAllProducts());
    }

    @Test
    void getAllProducts_singleProductIsNull(){
        List<Product> products = List.of(
                new Product("Milk"),
                new Product(null),
                new Product("Sugar")
        );

        when(repository.findAllProducts()).thenReturn(products);

        List<Product> expectedProducts = service.getAllProducts();

        assertEquals(3,expectedProducts.size());

    }

}