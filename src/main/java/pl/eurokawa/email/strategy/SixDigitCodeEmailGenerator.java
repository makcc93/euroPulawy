package pl.eurokawa.email.strategy;

import pl.eurokawa.email.EmailType;

public class SixDigitCodeEmailGenerator implements EmailGeneratorStrategy{
    @Override
    public boolean supports(EmailType emailType) {
        return emailType == EmailType.SIX_DIGIT_CODE;
    }

    @Override
    public String generateEmailSubject() {
        return "KOD AUTORYZACJI W SERWISIE EUROPULAWY.PL";
    }

    @Override
    public String generateEmailBody() {
        return "Działanie na stronie wymaga autoryzacji.\n\nTwój kod to:\n\n" ;
    }
}
