package pl.eurokawa.email.strategy;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.eurokawa.email.EmailServiceImpl;
import pl.eurokawa.email.EmailType;
import pl.eurokawa.user.User;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceImplTest {

    @Spy
    EmailServiceImpl spy = spy(EmailServiceImpl.class);

    @Test
    void sendSixNumbersCodeSendingTest() {
        
    }
    //koncze na tym ze weszka refaktoryzacja kodu i teraz czas na restowanie

    /*
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

     */
}