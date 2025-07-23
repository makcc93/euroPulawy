package pl.eurokawa.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pl.eurokawa.balance.Balance;
import pl.eurokawa.other.*;
import pl.eurokawa.purchase.Purchase;
import pl.eurokawa.terms.TermsOfService;
import pl.eurokawa.token.Token;
import pl.eurokawa.transaction.Transaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
public class User extends AbstractEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String firstName;

    private String lastName;

    @Email
    private String email;

    @Column(length = 60)
    private String password;

    private String role;

    private Boolean isCoffeeMember;

    private Boolean emailConfirmed;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Purchase> purchases;

    @OneToMany(mappedBy = "user")
    private List<Transaction> transactions;

    @OneToMany(mappedBy = "user")
    private List<Balance> balanceOperations;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Token> tokens = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<TermsOfService> userUploadedTerms;

    public User (){}

    public User(String firstName, String lastName, String email, String password){
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.role = UserType.NOTCONFIRMED.name();
        this.isCoffeeMember = false;
        this.emailConfirmed = false;
    }

    @Override
    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String normalizeRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;

        return List.of(new SimpleGrantedAuthority(normalizeRole));
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean getIsCoffeeMember() {
        return isCoffeeMember;
    }

    public void setIsCoffeeMember(boolean coffeeMember) {
        isCoffeeMember = coffeeMember;
    }

    @Override
    public String toString(){
        return firstName +" "+lastName;
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public void setTokens(List<Token> tokens) {
        this.tokens = tokens;
    }

    public boolean getEmailConfirmed() {
        return emailConfirmed;
    }

    public void setEmailConfirmed(Boolean emailConfirmed) {
        this.emailConfirmed = emailConfirmed;
    }
}
