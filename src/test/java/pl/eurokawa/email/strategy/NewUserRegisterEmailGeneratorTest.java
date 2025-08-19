package pl.eurokawa.email.strategy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.eurokawa.email.EmailType;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class NewUserRegisterEmailGeneratorTest {

    @InjectMocks
    NewUserRegisterEmailGenerator newUserRegisterEmailGenerator;

    @Test
    void supports_workingTest(){
        EmailType emailType = EmailType.NEW_USER_REGISTER;

        boolean doSupport = newUserRegisterEmailGenerator.supports(emailType);
        assertTrue(doSupport);
    }

    @Test
    void supports_EmailConfirmationShouldNotPassed(){
        EmailType emailType = EmailType.EMAIL_CONFIRMATION;
        boolean doSupport = newUserRegisterEmailGenerator.supports(emailType);

        assertFalse(doSupport);
    }

    @Test
    void supports_EmailTypeIsNull(){
        EmailType emailType = null;
        NullPointerException exception = assertThrows(NullPointerException.class, () -> newUserRegisterEmailGenerator.supports(emailType));

        assertEquals("Email type cannot be null", exception.getMessage());
    }

    @Test
    void generateEmailSubject_WorkingTest(){
        assertEquals("[ADMIN_ONLY] Zarejestrował się nowy użytkownik",newUserRegisterEmailGenerator.generateEmailSubject());
    }

    @Test
    void generateEmailBody_WorkingTest(){
        String url = "google.com";

        String expectedBodyValue = newUserRegisterEmailGenerator.generateEmailBody(url);

        assertEquals("Właśnie zarejestrował się nowy użytkownik.\n\n" +
                "Sprawdź jego autentyczność i nadaj mu odpowiednie uprawnienia.\n\n" +
                "Jeśli od razu chcesz potwierdzić nową rejestrację kliknij w link poniżej:\n\n\""
                + url,

                expectedBodyValue);
    }

    @Test
    void generateEmailBody_UrlIsNull(){
        String url = null;

        NullPointerException exception = assertThrows(NullPointerException.class, () -> newUserRegisterEmailGenerator.generateEmailBody(url));

        assertEquals("Token cannot be null",exception.getMessage());
    }
}