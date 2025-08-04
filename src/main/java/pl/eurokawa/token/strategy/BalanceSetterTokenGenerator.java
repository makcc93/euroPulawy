package pl.eurokawa.token.strategy;

import org.springframework.stereotype.Component;
import pl.eurokawa.token.TokenType;
@Component
public class BalanceSetterTokenGenerator implements TokenGeneratorStrategy{
    @Override
    public boolean supports(TokenType tokenType) {
        return tokenType == TokenType.BALANCE_SETTER;
    }

    @Override
    public String generateTokenValue() {
        return RandomCodeGenerator.randomDigits(6);
    }
}
