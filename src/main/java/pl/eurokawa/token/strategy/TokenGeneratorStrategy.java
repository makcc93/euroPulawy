package pl.eurokawa.token.strategy;

import pl.eurokawa.token.TokenType;

public interface TokenGeneratorStrategy {
    boolean supports(TokenType tokenType);
    String generateTokenValue();
}
