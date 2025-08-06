package pl.eurokawa.email;

import com.vaadin.flow.router.NotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import pl.eurokawa.email.strategy.EmailGeneratorStrategy;
import pl.eurokawa.exception.ArgumentNullChecker;

import java.util.List;

@Service
public class EmailContentFactory {
    private final List<EmailGeneratorStrategy> strategies;

    public EmailContentFactory(List<EmailGeneratorStrategy> strategies) {
        this.strategies = strategies;
    }

    public String generateSubjectValue(EmailType emailType){
        ArgumentNullChecker.check(emailType,"Email type");

        return findStrategy(emailType).generateEmailSubject();
    }

    public String generateBodyValue(EmailType emailType, String token){
        ArgumentNullChecker.check(emailType,"Email type");
        ArgumentNullChecker.check(token,"Token");

        return findStrategy(emailType).generateEmailBody(token);
    }

    private EmailGeneratorStrategy findStrategy(EmailType emailType){
        return strategies.stream()
                .filter(strategy -> strategy.supports(emailType))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Cannot find email strategy by email type: " +  emailType));
    }
}
