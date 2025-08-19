package pl.eurokawa.email;


import com.vaadin.flow.router.NotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.eurokawa.exception.ArgumentNullChecker;
import pl.eurokawa.token.TokenService;
import pl.eurokawa.token.TokenType;
import pl.eurokawa.user.User;
import pl.eurokawa.user.UserService;
import pl.eurokawa.user.UserType;

@RestController
public class EmailConfirmationController {
    private static final Logger log = LogManager.getLogger(EmailConfirmationController.class);
    private final UserService userService;
    private final TokenService tokenService;

    public EmailConfirmationController(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @GetMapping("/users/{id}/emailConfirmation/{token}")
    public ResponseEntity<String> confirmUserEmail(@PathVariable Integer id,@PathVariable String token){
        ArgumentNullChecker.check(id,token);

        try {
            User user = userService.getById(id);
            String userLastRegistrationToken = tokenService.getLastUserTokenByType(user.getId(), TokenType.REGISTRATION).getValue();

            if (userLastRegistrationToken.equals(token)) {
                user.setEmailConfirmed(true);
                userService.save(user);

                log.info("mail potwierdzony dla {}, a uzyty token = {}", user.getEmail(), token);
                return ResponseEntity.ok("Twój email został poprawnie potwierdzony!");
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Niepoprawny lub przedawniony token!");
        }
        catch (NotFoundException exception){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Użytkownik nie znaleziony");
        }
    }

    @GetMapping("/users/admin-account-confirmation/{userId}/{token}")
    public ResponseEntity<String> confirmUserAccount(@PathVariable Integer userId, @PathVariable String token){
        String tokenInUserEmail = tokenService.getLastUserTokenByType(userId,TokenType.ACCOUNT_CONFIRMATION).getValue();
        if (!token.equals(tokenInUserEmail)){
            log.warn("Controller: admin-account-confirmation, token = {}, tokenRepository = {}",token,tokenInUserEmail);
            return ResponseEntity.badRequest().build();
        }

        User userById = userService.getById(userId);
        log.info("Controller: admin-account-confirmation, user = {}",userById);
        log.info("Controller: admin-account-confirmation, role = {}",userById.getRole());

        userById.setRole(UserType.USER.name());
        log.info("Controller: admin-account-confirmation, role after= {}, test .name = {}",userById.getRole(),UserType.USER.name());
        userService.save(userById);

        return ResponseEntity.ok("Konto zostało poprawnie potwierdzone!");
    }
}
