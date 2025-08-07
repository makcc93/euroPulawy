package pl.eurokawa.email.strategy;

import org.springframework.stereotype.Component;
import pl.eurokawa.email.EmailType;
import pl.eurokawa.exception.ArgumentNullChecker;

@Component
public class NewUserRegisterEmailGenerator implements EmailGeneratorStrategy{
    @Override
    public boolean supports(EmailType emailType) {
        ArgumentNullChecker.check(emailType,"Email type");
        return emailType == EmailType.NEW_USER_REGISTER;
    }

    @Override
    public String generateEmailSubject() {
        return "[ADMIN_ONLY] Zarejestrował się nowy użytkownik";
    }

    @Override
    public String generateEmailBody(String url) {
        ArgumentNullChecker.check(url,"Token");

        return "Właśnie zarejestrował się nowy użytkownik.\n\n" +
                "Sprawdź jego autentyczność i nadaj mu odpowiednie uprawnienia.\n\n" +
                "Jeśli od razu chcesz potwierdzić nową rejestrację kliknij w link poniżej:\n\n\""
                + url;
    }
}
