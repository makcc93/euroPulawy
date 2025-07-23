package pl.eurokawa.transaction;

import jakarta.persistence.*;
import pl.eurokawa.purchase.Purchase;
import pl.eurokawa.user.User;


import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private BigDecimal amount;

    @OneToOne
    @JoinColumn(name = "purchase_id")
    private Purchase purchase;

    @Column(name = "created_at", nullable = false,updatable = false)
    private LocalDateTime createdAt;

    private Boolean isSaved;

    private Boolean isConfirmed;

    public Transaction(){}

    public Transaction(User user,TransactionType type, BigDecimal amount){
        this.user = user;
        this.type = type;
        this.amount = amount;
        this.isSaved = false;
        this.isConfirmed = false;
    }

    @PrePersist
    protected void onCreate(){
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Purchase getPurchase() {
        return purchase;
    }

    public void setPurchase(Purchase purchase) {
        this.purchase = purchase;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getSaved() {
        return isSaved;
    }

    public void setSaved(Boolean saved) {
        isSaved = saved;
    }

    public Boolean getConfirmed() {
        return isConfirmed;
    }

    public void setConfirmed(Boolean confirmed) {
        isConfirmed = confirmed;
    }
}
