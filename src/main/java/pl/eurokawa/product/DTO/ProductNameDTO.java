package pl.eurokawa.product.DTO;

import jakarta.validation.constraints.NotBlank;
import pl.eurokawa.exception.ArgumentNullChecker;
import pl.eurokawa.product.Product;

public record ProductNameDTO(
        @NotBlank(message = "Product name cannot be empty")
        String name
) {
    public static ProductNameDTO from(Product product){
        ArgumentNullChecker.check(product,"Product");

        return new ProductNameDTO(product.getName());
    }
}
