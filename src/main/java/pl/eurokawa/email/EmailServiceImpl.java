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
    private final EmailContentFactory emailContentFactory;

    public EmailServiceImpl(JavaMailSender javaMailSender,
                            @Value("${spring.mail.username}") String fromAddress, UserService userService, EmailContentFactory emailContentFactory){
        this.javaMailSender = javaMailSender;
        this.fromAddress = fromAddress;
        this.userService = userService;
        this.emailContentFactory = emailContentFactory;
    }

    @Override
    public void sendSixNumbersCode(EmailType emailType, User user, String code){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(user.getEmail());
        message.setSubject(emailContentFactory.generateSubjectValue(emailType));
        message.setText(emailContentFactory.generateBodyValue(emailType,code));

        javaMailSender.send(message);
    }

    @Override
    public void sendEmailConfirmationLink(EmailType emailType,User user, String token){
        String url = "http://europulawy.pl/users/" + user.getId() + "/emailConfirmation/" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(user.getEmail());
        message.setSubject(emailContentFactory.generateSubjectValue(emailType));
        message.setText(emailContentFactory.generateBodyValue(emailType,url));

        javaMailSender.send(message);
        log.info("EMAILservice, sendEmailConfirmationLink do: {}", user);
    }

    @Override
    public void sendEmailNotificationToAdmins (EmailType emailType, User user, String token){
        String url = "http://europulawy.pl/users/admin-account-confirmation/" + user.getId() + "/" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setSubject(emailContentFactory.generateSubjectValue(emailType));
        message.setText(emailContentFactory.generateBodyValue(emailType,url));

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
}
