package pl.eurokawa.email.strategy;

import org.springframework.stereotype.Component;
import pl.eurokawa.email.EmailType;
import pl.eurokawa.user.User;

@Component
public class NewUserRegisterEmailGenerator implements EmailGeneratorStrategy{
    @Override
    public boolean supports(EmailType emailType) {
        return emailType == EmailType.NEW_USER_REGISTER;
    }

    @Override
    public String generateEmailSubject() {
        return "[ADMIN_ONLY] Zarejestrował się nowy użytkownik: ";
    }

    @Override
    public String generateEmailBody() {
        return """
                        Właśnie zarejestrował się nowy użytkownik.
                        
                        Sprawdź jego autentyczność i nadaj mu odpowiednie uprawnienia!
                        
                        Jeśli po samych danych chcesz zatwierdzić użytkownika kliknij poniższy link:
                        
                      """;
    }
}
