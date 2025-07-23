package pl.eurokawa.email;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import pl.eurokawa.user.User;
import pl.eurokawa.user.UserRepository;
import pl.eurokawa.user.UserService;
import pl.eurokawa.user.UserType;

import java.util.ArrayList;
import java.util.List;

@Service
public class EmailService {
    private static final Logger log = LogManager.getLogger(EmailService.class);
    private final JavaMailSender javaMailSender;
    private final String fromAddress;
    private final UserService userService;

    public EmailService(JavaMailSender javaMailSender,
                        @Value("${spring.mail.username}") String fromAddress, UserService userService, UserRepository userRepository){
        this.javaMailSender = javaMailSender;
        this.fromAddress = fromAddress;
        this.userService = userService;
    }

    public void sendSixNumbersCode(String to,String code){
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject("KOD AUTORYZACJI W SERWISIE EUROPULAWY.PL");
        message.setText("Twój kod autoryzacji to:\n\n" + code + "\n\nKod wprowadź na stronie w odpowiednim oknie.\n\nEmail wygenerowany automatycznie, prosimy na niego nie odpowiadać.");

        javaMailSender.send(message);
        log.info("EMAILservice, sendSixNumbersCode do: " + to);
    }

    public void sendEmailConfirmationLink(String to, String token){
        String userId = String.valueOf(userService.getUserByEmail(to).orElseThrow().getId());
        String web = "http://europulawy.pl/users/" + userId +"/emailConfirmation/" + token;

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject("POTWIERDZENIE EMAILA W SERWISIE EUROPULAWY.PL");
        message.setText("Dziękujemy za rejestrację w serwisie europulawy.pl!\nWejdź w poniższy link, aby potwierdzić email swojego konta:\n\n" + web + "\n\nEmail wygenerowany automatycznie, prosimy na niego nie odpowiadać.");

        javaMailSender.send(message);
        log.info("EMAILservice, sendEmailConfirmationLink do: " + to);
    }

    public void sendEmailNotificationToAdmins (EmailType emailType, User user, String token){
        StringBuilder subject = new StringBuilder();
        StringBuilder body = new StringBuilder();

        String web = "http://europulawy.pl/users/admin-account-confirmation/" + user.getId() + "/" + token;

        switch (emailType){
            case NEW_USER_REGISTER -> {
                subject.append("[ADMIN_ONLY] Zarejestrował się nowy użytkownik: ").append(user.toString());
                body.append("""
                        Właśnie zarejestrował się nowy użytkownik.
                        
                        Sprawdź jego autentyczność i nadaj mu odpowiednie uprawnienia!
                        
                        Jeśli po samych danych chcesz zatwierdzić użytkownika kliknij poniższy link:
                        
                      """)
                        .append(web);
            }
            default -> throw new IllegalArgumentException("This email type does not exist!");
        }

        List<User> admins = new ArrayList<>();
        for (User singleUser : userService.getAll()){
            if (singleUser.getRole().equals(UserType.ADMIN.name())){
                admins.add(singleUser);
            }
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setSubject(subject.toString());
        message.setText(body.toString());

        for (User admin : admins){
            message.setTo(admin.getEmail());
            log.info("EmailSERVICE wysylka maila po rejestracji dla uzytkownika (ADMINA): {}",admin.getEmail() );
        }

        try {
            javaMailSender.send(message);
            log.info("EmailSERVICE wysylka maila powiodła się, message = {}",message);
        }
        catch (Exception e){
            throw new RuntimeException("Cannot send email notification to admins! " + e);
        }
    }
}
