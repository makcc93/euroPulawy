package pl.eurokawa.email.strategy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.eurokawa.email.EmailType;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AccountConfirmationEmailGeneratorTest {

    @InjectMocks
    AccountConfirmationEmailGenerator generator;
    @Test
    void supports_WorkingTest(){
        EmailType emailType = EmailType.EMAIL_CONFIRMATION;
        boolean doSupport = generator.supports(emailType);

        assertTrue(doSupport);
    }

    @Test
    void supports_SixDigitCodeShouldNotPassed(){
        EmailType emailType = EmailType.SIX_DIGIT_CODE;
        boolean doSupport = generator.supports(emailType);

        assertFalse(doSupport);
    }

    @Test
    void supports_EmailTypeIsNull(){
        EmailType emailType = null;
        NullPointerException exception = assertThrows(NullPointerException.class, () -> generator.supports(emailType));

        assertEquals("Email type cannot be null",exception.getMessage());
    }

    @Test
    void generateEmailSubject_WorkingTest(){
        assertEquals("POTWIERDZENIE EMAILA W SERWISIE EUROPULAWY.PL",
                generator.generateEmailSubject());
    }

    @Test
    void generateEmailBody_WorkingTest(){
        String token = "google.com";

        assertEquals("Dziękujemy za rejestrację w serwisie europulawy.pl!\n" +
                        "Wejdź w poniższy link, aby potwierdzić email swojego konta:\n\n" +
                        token

                ,generator.generateEmailBody(token));
    }

    @Test
    void generateEmailBody_TokenIsNull(){
        String token = null;
        NullPointerException exception = assertThrows(NullPointerException.class, () -> generator.generateEmailBody(token));

        assertEquals("Token cannot be null",exception.getMessage());
    }
}