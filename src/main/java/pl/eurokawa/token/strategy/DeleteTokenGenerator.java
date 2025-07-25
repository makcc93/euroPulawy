package pl.eurokawa.token.strategy;

import org.springframework.stereotype.Component;
import pl.eurokawa.token.TokenType;

@Component
public class DeleteTokenGenerator implements TokenGeneratorStrategy{

    @Override
    public boolean supports(TokenType tokenType) {
        return tokenType == TokenType.DELETE;
    }

    @Override
    public String generateTokenValue() {
        return RandomCodeGenerator.randomDigits(6);
    }
}
