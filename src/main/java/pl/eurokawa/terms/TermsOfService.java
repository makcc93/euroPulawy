package pl.eurokawa.terms;

import jakarta.persistence.*;
import org.springframework.stereotype.Component;
import pl.eurokawa.user.User;

import java.time.LocalDateTime;

@Entity
@Component
public class TermsOfService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "file_name")
    private String fileName;

    @ManyToOne
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @Column(name = "created_at",nullable = false,updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_actual",nullable = false)
    private Boolean isActual;

    @PrePersist
    protected void onCreate(){ createdAt = LocalDateTime.now();}

    public TermsOfService(){}

    public TermsOfService(String fileName,User user){
        this.fileName = fileName;
        this.user = user;
        this.isActual = false;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getActual() {
        return isActual;
    }

    public void setActual(Boolean actual) {
        isActual = actual;
    }
}
