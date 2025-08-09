package pl.eurokawa.user.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.NotBlank;

public record RegisterUserRequest(
        @NotBlank(message = "Imię jest wymagane")
        String firstName,
     
        @NotBlank(message = "Nazwisko jest wymagane")
        String lastName,
     
        @NotBlank(message = "Email jest wymagany")
        @Email(message = "Nieprawidłowy format email")
        String email,
     
        @NotBlank(message = "Hasło jest wymagane")
        @Size(min = 6, message = "Hasło musi mieć min. 6 znaków")
        String password
        ) {}
