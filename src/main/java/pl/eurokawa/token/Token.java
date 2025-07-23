package pl.eurokawa.token;

import jakarta.persistence.*;
import pl.eurokawa.user.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "token")
public class Token{

    public Token(){}

    public Token(User user, String value, TokenType type){
        this.user = user;
        this.value = value;
        this.type = type;
    }

    public Token(User user, String value, TokenType type,LocalDateTime expiryDate){
        this.user = user;
        this.value = value;
        this.type = type;
        this.expiryDate = expiryDate;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255, unique = true)
    private String value;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime expiryDate;

    @Enumerated(EnumType.STRING)
    private TokenType type;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public TokenType getType() {
        return type;
    }

    public void setType(TokenType type) {
        this.type = type;
    }
}
