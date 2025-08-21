package pl.eurokawa.product;

import com.vaadin.flow.router.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.eurokawa.product.DTO.ProductNameDTO;

import java.util.List;
import java.util.Optional;

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

        NotFoundException exception = assertThrows(NotFoundException.class, () -> service.getAllProducts());

        assertEquals("Cannot find any products!",exception.getMessage());
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

    @Test
    void create_workingTest(){
        String productName = "Milk";

        Product product = new Product();
        product.setName(productName);

        when(repository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductNameDTO productNameDTO = service.create(productName);

        assertEquals(productName,productNameDTO.name());
    }

    @Test
    void create_productNameIsNull(){
        String productName = null;

        assertThrows(NullPointerException.class, () -> service.create(productName));
    }

    @Test
    void create_productNameIsEmpty(){
        String productName = "";

        Product product = new Product();
        product.setName(productName);

        when(repository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductNameDTO productNameDTO = service.create(productName);

        assertEquals("",productNameDTO.name());
    }

    @Test
    void save_workingTest(){
        Product product = new Product("Water");

        when(repository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductNameDTO saved = service.save(product);

        assertEquals("Water",saved.name());
    }

    @Test
    void save_productIsNull(){
        Product product = null;

        NullPointerException exception = assertThrows(NullPointerException.class, () -> service.save(product));

        assertEquals("Product cannot be null",exception.getMessage());
        verify(repository,never()).save(any(Product.class));
    }

    @Test
    void delete_workingTest(){
        Integer id = 1;
        Product product = mock(Product.class);
        product.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(product));

        service.delete(id);

        verify(repository,times(1)).delete(product);
    }

    @Test
    void delete_idIsNull(){
        Integer id = null;

        NullPointerException exception = assertThrows(NullPointerException.class, () -> service.delete(id));

        assertEquals("Id cannot be null",exception.getMessage());
    }

    @Test
    void delete_productWithThisIdDoesNotExist(){
        Integer id = 1;

        when(repository.findById(id)).thenThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> service.delete(id));
    }

    @Test
    void findById_workingTest(){
        Integer id = 2222;
        Product product = new Product();
        product.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(product));

        Product responseProduct = service.findById(id);

        assertEquals(2222,responseProduct.getId());
    }

    @Test
    void findById_idIsNull(){
        NullPointerException exception = assertThrows(NullPointerException.class, () -> service.findById(null));

        assertEquals("Id cannot be null",exception.getMessage());
    }

    @Test
    void findById_cannotFindProductById(){
        Integer id = 1234;
        when(repository.findById(1234)).thenThrow(new NotFoundException("Product with this id does not exist!"));

        NotFoundException exception = assertThrows(NotFoundException.class, () -> service.findById(id));

        assertEquals("Product with this id does not exist!", exception.getMessage());
    }

}