package pl.eurokawa.email;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.eurokawa.token.TokenRepository;
import pl.eurokawa.user.User;
import pl.eurokawa.user.UserRepository;
import pl.eurokawa.user.UserService;
import pl.eurokawa.user.UserType;

import java.util.Optional;

@RestController
public class EmailConfirmationController {
    private static final Logger log = LogManager.getLogger(EmailConfirmationController.class);
    private final UserService userService;
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;

    public EmailConfirmationController(UserService userService, TokenRepository tokenRepository, UserRepository userRepository) {
        this.userService = userService;
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/users/{id}/emailConfirmation/{token}")
    public ResponseEntity<String> confirmUserEmail(@PathVariable Integer id,@PathVariable String token){
        User user = userService.getUserById(id).orElseThrow();
        String userLastRegistrationToken = tokenRepository.findUserLastRegistrationToken(user.getId()).getValue();

        if (userLastRegistrationToken.equals(token)) {
            user.setEmailConfirmed(true);
            userRepository.save(user);

            log.info("mail potwierdzony dla " + user.getEmail() + "    a uzyty token = " + token);
            return ResponseEntity.ok("Twój email został poprawnie potwierdzony!");
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Niepoprawny lub przedawniony token!");
    }

    @GetMapping("/users/admin-account-confirmation/{userId}/{token}")
    public ResponseEntity<String> confirmUserAccount(@PathVariable Integer userId, @PathVariable String token){
        String tokenInUserEmail = tokenRepository.findUserLastAccountConfirmationToken(userId).getValue();
        if (!token.equals(tokenInUserEmail)){
            log.warn("Controller: admin-account-confirmation, token = {}, tokenRepository = {}",token,tokenInUserEmail);
            return ResponseEntity.badRequest().build();
        }

        User userById = userRepository.findUserById(userId).orElseThrow();
        log.info("Controller: admin-account-confirmation, user = {}",userById);
        log.info("Controller: admin-account-confirmation, role = {}",userById.getRole());

        userById.setRole(UserType.USER.name());
        log.info("Controller: admin-account-confirmation, role after= {}, test .name = {}",userById.getRole(),UserType.USER.name());
        userRepository.save(userById);

        return ResponseEntity.ok("Konto zostało poprawnie potwierdzone!");
    }
}
