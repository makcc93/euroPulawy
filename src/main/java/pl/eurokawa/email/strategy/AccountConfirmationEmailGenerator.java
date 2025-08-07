package pl.eurokawa.email.strategy;

import org.springframework.stereotype.Component;
import pl.eurokawa.email.EmailType;
import pl.eurokawa.exception.ArgumentNullChecker;

@Component
public class AccountConfirmationEmailGenerator implements  EmailGeneratorStrategy{
    @Override
    public boolean supports(EmailType emailType) {
        ArgumentNullChecker.check(emailType,"Email type");

        return emailType == EmailType.EMAIL_CONFIRMATION;
    }

    @Override
    public String generateEmailSubject() {
        return "POTWIERDZENIE EMAILA W SERWISIE EUROPULAWY.PL";
    }

    @Override
    public String generateEmailBody(String url) {
        ArgumentNullChecker.check(url,"Token");

        return "Dziękujemy za rejestrację w serwisie europulawy.pl!\nWejdź w poniższy link, aby potwierdzić email swojego konta:\n\n" + url;
    }
}
