package pl.eurokawa.token.strategy;

import org.springframework.stereotype.Component;
import pl.eurokawa.token.TokenType;

import java.util.UUID;
@Component
public class RegistrationTokenGenerator implements TokenGeneratorStrategy{
    @Override
    public boolean supports(TokenType tokenType) {
        return tokenType == TokenType.REGISTRATION;
    }

    @Override
    public String generateTokenValue() {
        return UUID.randomUUID().toString();
    }
}
