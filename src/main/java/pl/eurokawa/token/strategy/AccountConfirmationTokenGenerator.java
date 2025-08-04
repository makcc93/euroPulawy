package pl.eurokawa.token.strategy;

import org.springframework.stereotype.Component;
import pl.eurokawa.token.TokenType;

import java.util.UUID;

@Component
public class AccountConfirmationTokenGenerator implements TokenGeneratorStrategy{
    @Override
    public boolean supports(TokenType tokenType) {
        return tokenType == TokenType.ACCOUNT_CONFIRMATION;
    }

    @Override
    public String generateTokenValue() {
        return UUID.randomUUID().toString();
    }
}
