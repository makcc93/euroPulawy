package pl.eurokawa.email.strategy;

import pl.eurokawa.email.EmailType;

public interface EmailGeneratorStrategy {
    boolean supports(EmailType emailType);
    String generateEmailSubject();
    String generateEmailBody(String token);
}
