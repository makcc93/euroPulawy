package pl.eurokawa.token.strategy;

import org.springframework.stereotype.Component;
import pl.eurokawa.token.TokenType;

import java.util.Random;

@Component
public class PasswordResetTokenGenerator implements TokenGeneratorStrategy {
    @Override
    public boolean supports(TokenType tokenType) {

        return tokenType == TokenType.PASSWORD_RESET;
    }

    @Override
    public String generateTokenValue() {
        return RandomCodeGenerator.randomDigits(6);
    }
}
