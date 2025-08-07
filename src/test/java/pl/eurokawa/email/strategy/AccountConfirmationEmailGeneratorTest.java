package pl.eurokawa.email.strategy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.eurokawa.email.EmailType;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AccountConfirmationEmailGeneratorTest {

    @Test
    void supports_WorkingTest(){
        EmailType emailType = EmailType.EMAIL_CONFIRMATION;
        AccountConfirmationEmailGenerator accountConfirmationEmailGenerator = new AccountConfirmationEmailGenerator();
        boolean doSupport = accountConfirmationEmailGenerator.supports(emailType);

        assertTrue(doSupport);
    }

    @Test
    void supports_SixDigitCodeShouldNotPassed(){
        EmailType emailType = EmailType.SIX_DIGIT_CODE;
        AccountConfirmationEmailGenerator accountConfirmationEmailGenerator = new AccountConfirmationEmailGenerator();
        boolean doSupport = accountConfirmationEmailGenerator.supports(emailType);

        assertFalse(doSupport);
    }

    @Test
    void supports_EmailTypeIsNull(){
        EmailType emailType = null;
        AccountConfirmationEmailGenerator accountConfirmationEmailGenerator = new AccountConfirmationEmailGenerator();
        NullPointerException exception = assertThrows(NullPointerException.class, () -> accountConfirmationEmailGenerator.supports(emailType));

        assertEquals("Email type cannot be null",exception.getMessage());
    }

    @Test
    void generateEmailSubject_WorkingTest(){
        AccountConfirmationEmailGenerator accountConfirmationEmailGenerator = new AccountConfirmationEmailGenerator();

        assertEquals("POTWIERDZENIE EMAILA W SERWISIE EUROPULAWY.PL",
                accountConfirmationEmailGenerator.generateEmailSubject());
    }

    @Test
    void generateEmailBody_WorkingTest(){
        String token = "google.com";
        AccountConfirmationEmailGenerator accountConfirmationEmailGenerator = new AccountConfirmationEmailGenerator();

        assertEquals("Dziękujemy za rejestrację w serwisie europulawy.pl!\n" +
                        "Wejdź w poniższy link, aby potwierdzić email swojego konta:\n\n" +
                        token

                ,accountConfirmationEmailGenerator.generateEmailBody(token));
    }

    @Test
    void generateEmailBody_TokenIsNull(){
        String token = null;
        AccountConfirmationEmailGenerator accountConfirmationEmailGenerator = new AccountConfirmationEmailGenerator();
        NullPointerException exception = assertThrows(NullPointerException.class, () -> accountConfirmationEmailGenerator.generateEmailBody(token));

        assertEquals("Token cannot be null",exception.getMessage());
    }

}
/*
@Component
public class AccountConfirmationEmailGenerator implements  EmailGeneratorStrategy{
    @Override
    public boolean supports(EmailType emailType) {
        return emailType == EmailType.EMAIL_CONFIRMATION;
    }

    @Override
    public String generateEmailSubject() {
        return "POTWIERDZENIE EMAILA W SERWISIE EUROPULAWY.PL";
    }

    @Override
    public String generateEmailBody(String url) {
        return "Dziękujemy za rejestrację w serwisie europulawy.pl!\nWejdź w poniższy link, aby potwierdzić email swojego konta:\n\n" + url;
    }
}
 */