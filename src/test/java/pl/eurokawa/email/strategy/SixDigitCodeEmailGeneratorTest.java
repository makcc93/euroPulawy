package pl.eurokawa.email.strategy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.eurokawa.email.EmailType;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SixDigitCodeEmailGeneratorTest {

    @InjectMocks
    SixDigitCodeEmailGenerator sixDigitCodeEmailGenerator;

    @Test
    void supports_WorkingTest(){
        EmailType emailType = EmailType.SIX_DIGIT_CODE;
        boolean doSupport = sixDigitCodeEmailGenerator.supports(emailType);

        assertTrue(doSupport);
    }

    @Test
    void supports_NewUserRegisterShouldNotPassed(){
        EmailType emailType = EmailType.NEW_USER_REGISTER;
        boolean doSupport = sixDigitCodeEmailGenerator.supports(emailType);

        assertFalse(doSupport);
    }

    @Test
    void supports_EmailTypeIsNull(){
        EmailType emailType = null;
        NullPointerException exception = assertThrows(NullPointerException.class, () -> sixDigitCodeEmailGenerator.supports(emailType));

        assertEquals("Email type cannot be null", exception.getMessage());
    }

    @Test
    void generateEmailSubject_WorkingTest(){
        assertEquals("KOD AUTORYZACJI W SERWISIE EUROPULAWY.PL",
                sixDigitCodeEmailGenerator.generateEmailSubject());
    }

    @Test
    void generateEmailBody_WorkingTest(){
        String token = "123456";

        assertEquals("Działanie na stronie wymaga autoryzacji.\n\nTwój kod to:\n\n" + token,
                sixDigitCodeEmailGenerator.generateEmailBody(token));
    }

    @Test
    void generateEmailBody_TokenIsNull(){
        String token = null;

        NullPointerException exception = assertThrows(NullPointerException.class, () -> sixDigitCodeEmailGenerator.generateEmailBody(token));

        assertEquals("Token cannot be null", exception.getMessage());
    }
}