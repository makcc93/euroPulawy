package pl.eurokawa.purchase.DTO;

import org.springframework.lang.Nullable;

import java.math.BigDecimal;

public record CreatePurchaseRequest(
        Integer productId,
        BigDecimal price,
        Integer quantity,
        @Nullable String receiptImagePath
        ) {}
