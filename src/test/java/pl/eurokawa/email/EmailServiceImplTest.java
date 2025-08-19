package pl.eurokawa.email;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import pl.eurokawa.email.strategy.EmailContentFactory;
import pl.eurokawa.user.User;
import pl.eurokawa.user.UserService;
import pl.eurokawa.user.UserType;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @InjectMocks
    EmailServiceImpl emailService;

    @Mock
    UserService userService;

    @Mock
    EmailContentFactory emailContentFactory;

    @Mock
    JavaMailSender javaMailSender;

    @Test
    void sendSixNumbersCode_SendingTest() {
        User user = new User();
        user.setEmail("test@test.com");
        String code = "123456";
        EmailType emailType = EmailType.SIX_DIGIT_CODE;

        when(emailContentFactory.generateSubjectValue(emailType)).thenReturn("Subject test");
        when(emailContentFactory.generateBodyValue(emailType,code)).thenReturn("Your code: " + code);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.sendSixNumbersCode(emailType,user,code);

        verify(javaMailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();
        assertEquals("test@test.com",message.getTo()[0]);
        assertEquals("Subject test",message.getSubject());
        assertEquals("Your code: 123456",message.getText());
    }

    @Test
    void sendSixNumbersCode_EmailTypeIsNull(){
        User user = new User();
        user.setEmail("test@test.com");

        EmailType emailType = null;
        EmailContentFactory contentFactory = new EmailContentFactory(Collections.emptyList());

        NullPointerException exception = assertThrows(NullPointerException.class, () -> contentFactory.generateSubjectValue(emailType));
        assertEquals("Email type cannot be null",exception.getMessage());
    }

    @Test
    void sendSixNumbersCode_UserIsNull(){
        User user = null;
        String code = "123456";
        EmailType emailType = EmailType.EMAIL_CONFIRMATION;

        NullPointerException exception = assertThrows(NullPointerException.class, () -> emailService.sendEmailConfirmationLink(emailType, user, code));
        assertEquals("User cannot be null",exception.getMessage());
    }

    @Test
    void sendSixNumbersCode_TokenIsNull(){
        User user = new User();
        String token = null;
        EmailType emailType = EmailType.EMAIL_CONFIRMATION;

        NullPointerException exception = assertThrows(NullPointerException.class, () -> emailService.sendEmailConfirmationLink(emailType, user, token));
        assertEquals("Token cannot be null",exception.getMessage());
    }

    @Test
    void sendEmailNotificationToAdmins_SendingTest(){
        User user = new User();
        user.setEmail("test@test.com");
        user.setRole(UserType.ADMIN.name());
        String code = "123456";
        EmailType emailType = EmailType.SIX_DIGIT_CODE;

        when(emailContentFactory.generateSubjectValue(emailType)).thenReturn("Subject test");
        when(emailContentFactory.generateBodyValue(emailType,code)).thenReturn("Your code: " + code);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.sendSixNumbersCode(emailType,user,code);

        verify(javaMailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();
        assertEquals("test@test.com",message.getTo()[0]);
        assertEquals("Subject test",message.getSubject());
        assertEquals("Your code: 123456",message.getText());
    }

    @Test
    void sendEmailNotificationToAdmins_AdminAbsenceEmailNotSend(){
        User user = new User();
        user.setRole(UserType.USER.name());

        user.setEmail("test@test.com");
        String token = "123456";
        EmailType emailType = EmailType.NEW_USER_REGISTER;

        List<User> users = List.of(user);

        when(userService.getAll()).thenReturn(users);

        emailService.sendEmailNotificationToAdmins(emailType, user, token);

        verify(javaMailSender,never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendEmailNotificationToAdmins_EmailTypeIsNull(){
        User user = new User();
        user.setEmail("test@test.com");

        EmailType emailType = null;
        EmailContentFactory contentFactory = new EmailContentFactory(Collections.emptyList());

        NullPointerException exception = assertThrows(NullPointerException.class, () -> contentFactory.generateSubjectValue(emailType));
        assertEquals("Email type cannot be null",exception.getMessage());
    }

    @Test
    void sendEmailNotificationToAdmins_UserIsNull(){
        User user = null;
        String code = "123456";
        EmailType emailType = EmailType.EMAIL_CONFIRMATION;

        NullPointerException exception = assertThrows(NullPointerException.class, () -> emailService.sendEmailNotificationToAdmins(emailType, user, code));
        assertEquals("User cannot be null",exception.getMessage());
    }

    @Test
    void sendEmailNotificationToAdmins_TokenIsNull(){
        User user = new User();
        String token = null;
        EmailType emailType = EmailType.EMAIL_CONFIRMATION;

        NullPointerException exception = assertThrows(NullPointerException.class, () -> emailService.sendEmailNotificationToAdmins(emailType, user, token));
        assertEquals("Token cannot be null",exception.getMessage());
    }
}