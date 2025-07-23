package pl.eurokawa.purchase;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import pl.eurokawa.other.AbstractEntity;
import pl.eurokawa.product.Product;
import pl.eurokawa.transaction.Transaction;
import pl.eurokawa.user.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name="purchase")
public class Purchase extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @OneToOne(mappedBy = "purchase")
    private Transaction transaction;

    private LocalDateTime date;

    private boolean isConfirmed;

    private boolean isSaved;

    private BigDecimal price;

    private Integer quantity;

    private BigDecimal total;

    @CreatedDate
    @Column(name = "created_at", nullable = false,updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "receipt_image_path")
    private String receiptImagePath;

    @PrePersist
    protected void onCreate(){
        createdAt = LocalDateTime.now();
    }

    public Purchase(){}

    public Purchase(User user, Product product, BigDecimal price, int quantity){
        this.user = user;
        this.product = product;
        this.price = price;
        this.quantity = quantity;
        this.isConfirmed = false;
        this.isSaved = false;
        this.date = LocalDateTime.now();
        this.total = price.multiply(BigDecimal.valueOf(quantity));
    }

    public Purchase(User user, Product product, BigDecimal price, int quantity,String receiptImagePath){
        this.user = user;
        this.product = product;
        this.price = price;
        this.quantity = quantity;
        this.isConfirmed = false;
        this.isSaved = false;
        this.date = LocalDateTime.now();
        this.total = price.multiply(BigDecimal.valueOf(quantity));
        this.receiptImagePath = receiptImagePath;
    }

    public void updateTotal(){
        this.total = this.price.multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }

    public void setConfirmed(boolean confirmed) {
        isConfirmed = confirmed;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public boolean isSaved() {
        return isSaved;
    }

    public void setSaved(boolean saved) {
        isSaved = saved;
    }

    public String getReceiptImagePath() {
        return receiptImagePath;
    }

    public void setReceiptImagePath(String receiptImagePath) {
        this.receiptImagePath = receiptImagePath;
    }
}
