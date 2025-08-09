package pl.eurokawa.email.strategy;

import javassist.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.eurokawa.email.EmailType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailContentFactoryTest {

    List<EmailGeneratorStrategy> strategies = List.of(
            new SixDigitCodeEmailGenerator(),
            new NewUserRegisterEmailGenerator(),
            new AccountConfirmationEmailGenerator()
    );

    @Test
    void generateSubjectValue_SixDigitCodeTest(){
        EmailType emailType = EmailType.SIX_DIGIT_CODE;

        EmailGeneratorStrategy mockStrategy = mock(EmailGeneratorStrategy.class);
        when(mockStrategy.supports(emailType)).thenReturn(true);
        when(mockStrategy.generateEmailSubject()).thenReturn("example subject");

        EmailContentFactory emailContentFactory = new EmailContentFactory(List.of(mockStrategy));
        emailContentFactory.generateSubjectValue(emailType);

        verify(mockStrategy,times(1)).generateEmailSubject();
    }

    @Test
    void generateSubjectValue_NewUserRegisterTest(){
        EmailType emailType = EmailType.NEW_USER_REGISTER;

        EmailGeneratorStrategy mockStrategy = mock(EmailGeneratorStrategy.class);
        when(mockStrategy.supports(emailType)).thenReturn(true);
        when(mockStrategy.generateEmailSubject()).thenReturn("subject");

        EmailContentFactory emailContentFactory = new EmailContentFactory(List.of(mockStrategy));
        emailContentFactory.generateSubjectValue(emailType);

        verify(mockStrategy,times(1)).generateEmailSubject();

    }

    @Test
    void generateSubjectValue_AccountConfirmationTest(){
        EmailType emailType = EmailType.EMAIL_CONFIRMATION;

        EmailGeneratorStrategy mockStrategy = mock(EmailGeneratorStrategy.class);
        when(mockStrategy.supports(emailType)).thenReturn(true);
        when(mockStrategy.generateEmailSubject()).thenReturn("test");

        EmailContentFactory emailContentFactory = new EmailContentFactory(List.of(mockStrategy));
        emailContentFactory.generateSubjectValue(emailType);

        verify(mockStrategy,times(1)).generateEmailSubject();

    }

    @Test
    void generateSubjectValue_EmailTypeIsNull(){
        EmailType emailType = null;

        EmailContentFactory emailContentFactory = new EmailContentFactory(strategies);

        assertThrows(NullPointerException.class, () -> emailContentFactory.generateSubjectValue(emailType));
    }

    @Test
    void generateBodyValue_SixDigitCodeTest(){
        EmailType emailType = EmailType.SIX_DIGIT_CODE;
        String token = "111222";

        EmailGeneratorStrategy mockStrategy = mock(EmailGeneratorStrategy.class);
        when(mockStrategy.supports(emailType)).thenReturn(true);

        EmailContentFactory emailContentFactory = new EmailContentFactory(List.of(mockStrategy));
        emailContentFactory.generateBodyValue(emailType,token);

        verify(mockStrategy).generateEmailBody("111222");
    }

    @Test
    void generateBodyValue_NewUserRegisterTest(){
        EmailType emailType = EmailType.NEW_USER_REGISTER;
        String token = "google.com";

        EmailGeneratorStrategy mockStrategy = mock(EmailGeneratorStrategy.class);
        when(mockStrategy.supports(emailType)).thenReturn(true);

        EmailContentFactory emailContentFactory = new EmailContentFactory(List.of(mockStrategy));
        emailContentFactory.generateBodyValue(emailType,token);

        verify(mockStrategy).generateEmailBody("google.com");
    }

    @Test
    void generateBodyValue_AccountConfirmationTest(){
        EmailType emailType = EmailType.EMAIL_CONFIRMATION;
        String token = "google.com";
        EmailGeneratorStrategy mockStrategy = mock(EmailGeneratorStrategy.class);
        when(mockStrategy.supports(emailType)).thenReturn(true);

        EmailContentFactory emailContentFactory = new EmailContentFactory(List.of(mockStrategy));
        emailContentFactory.generateBodyValue(emailType,token);

        verify(mockStrategy).generateEmailBody("google.com");
    }

    @Test
    void generateBodyValue_emailTypeIsNull(){
        EmailType emailType = null;
        String token = "123456";

        EmailContentFactory emailContentFactory = new EmailContentFactory(strategies);

        assertThrows(NullPointerException.class, () -> emailContentFactory.generateBodyValue(emailType, token));
    }

    @Test
    void generateBodyValue_TokenIsNull(){
        EmailType emailType = EmailType.SIX_DIGIT_CODE;
        String token = null;

        EmailContentFactory emailContentFactory = new EmailContentFactory(strategies);

        assertThrows(NullPointerException.class, () -> emailContentFactory.generateBodyValue(emailType, token));
    }

    @Test
    void generateSubjectValue_FindStrategyFilterCorrect(){
        EmailType type = EmailType.EMAIL_CONFIRMATION;

        EmailGeneratorStrategy firstNotSupported = mock(EmailGeneratorStrategy.class);
        when(firstNotSupported.supports(any())).thenReturn(false);

        EmailGeneratorStrategy secondNotSupported = mock(EmailGeneratorStrategy.class);
        when(secondNotSupported.supports(any())).thenReturn(false);

        EmailGeneratorStrategy supported = mock(EmailGeneratorStrategy.class);
        when(supported.supports(type)).thenReturn(true);

        EmailContentFactory emailContentFactory = new EmailContentFactory(List.of(firstNotSupported, secondNotSupported, supported));

        emailContentFactory.generateSubjectValue(type);

        verify(firstNotSupported,never()).generateEmailSubject();
        verify(secondNotSupported,never()).generateEmailSubject();
        verify(supported,times(1)).generateEmailSubject();
    }
}