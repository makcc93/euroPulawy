package pl.eurokawa.email;

import com.vaadin.flow.router.NotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import pl.eurokawa.email.strategy.EmailGeneratorStrategy;
import pl.eurokawa.user.User;
import pl.eurokawa.user.UserService;
import pl.eurokawa.user.UserType;

import java.util.List;

@Service
public class EmailServiceImpl implements EmailService{
    private static final Logger log = LogManager.getLogger(EmailServiceImpl.class);
    private final JavaMailSender javaMailSender;
    private final String fromAddress;
    private final UserService userService;
    private final List<EmailGeneratorStrategy> strategies;

    public EmailServiceImpl(JavaMailSender javaMailSender,
                            @Value("${spring.mail.username}") String fromAddress, UserService userService, List<EmailGeneratorStrategy> strategies){
        this.javaMailSender = javaMailSender;
        this.fromAddress = fromAddress;
        this.userService = userService;
        this.strategies = strategies;
    }

    @Override
    public void sendSixNumbersCode(EmailType emailType, User user, String code){
        String subject = generateSubjectValue(emailType);
        String bodyWithCode = generateBodyValue(emailType) + code;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(user.getEmail());
        message.setSubject(subject);
        message.setText(bodyWithCode);

        javaMailSender.send(message);
        log.info("EMAILservice, sendSixNumbersCode do: {}", user);
    }

    @Override
    public void sendEmailConfirmationLink(EmailType emailType,User user, String token){
        String url = "http://europulawy.pl/users/" + user.getId()+ "/emailConfirmation/" + token;

        String subjectWithUserInfo = generateSubjectValue(emailType) + user;

        String bodyWithUrl = generateBodyValue(emailType) + url;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(user.getEmail());
        message.setSubject(subjectWithUserInfo);
        message.setText(bodyWithUrl);

        javaMailSender.send(message);
        log.info("EMAILservice, sendEmailConfirmationLink do: {}", user);
    }

    @Override
    public void sendEmailNotificationToAdmins (EmailType emailType, User user, String token){
        String url = "http://europulawy.pl/users/admin-account-confirmation/" + user.getId() + "/" + token;

        String subjectWithUserInfo = generateSubjectValue(emailType) + user;
        String bodyWithUrl = generateBodyValue(emailType) + url;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setSubject(subjectWithUserInfo);
        message.setText(bodyWithUrl);

        for (User singleUser : userService.getAll()){
            if (singleUser.getRole().equals(UserType.ADMIN.name())){
                message.setTo(singleUser.getEmail());
                try {
                    javaMailSender.send(message);
                }
                catch (Exception e){
                    throw new RuntimeException("Cannot send email notification to admins: " + e);
                }
            }
        }
    }

    private String generateSubjectValue(EmailType emailType){
        return findStrategy(emailType).generateEmailSubject();
    }

    private String generateBodyValue(EmailType emailType){
        return findStrategy(emailType).generateEmailBody();
    }

    private EmailGeneratorStrategy findStrategy(EmailType emailType){
        return strategies.stream()
                .filter(strategy -> strategy.supports(emailType))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Cannot find email strategy by email type: " +  emailType));
    }
}
