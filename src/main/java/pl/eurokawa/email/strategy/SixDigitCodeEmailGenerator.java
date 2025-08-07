package pl.eurokawa.email.strategy;

import pl.eurokawa.email.EmailType;
import pl.eurokawa.exception.ArgumentNullChecker;

public class SixDigitCodeEmailGenerator implements EmailGeneratorStrategy{
    @Override
    public boolean supports(EmailType emailType) {
        ArgumentNullChecker.check(emailType,"Email type");

        return emailType == EmailType.SIX_DIGIT_CODE;
    }

    @Override
    public String generateEmailSubject() {
        return "KOD AUTORYZACJI W SERWISIE EUROPULAWY.PL";
    }

    @Override
    public String generateEmailBody(String token) {
        ArgumentNullChecker.check(token,"Token");
        return "Działanie na stronie wymaga autoryzacji.\n\nTwój kod to:\n\n" + token;
    }
}
