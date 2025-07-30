package pl.eurokawa.email.strategy;

import pl.eurokawa.email.EmailType;
import pl.eurokawa.user.User;

public interface EmailGeneratorStrategy {
    boolean supports(EmailType emailType);
    String generateEmailSubject();
    String generateEmailBody();
}
