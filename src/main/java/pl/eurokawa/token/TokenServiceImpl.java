package pl.eurokawa.token;

import com.vaadin.flow.router.NotFoundException;
import org.springframework.stereotype.Service;
import pl.eurokawa.token.strategy.TokenGeneratorStrategy;
import pl.eurokawa.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
public class TokenServiceImpl implements TokenService {
    private final List<TokenGeneratorStrategy> strategies;
    private final TokenRepository tokenRepository;

    public TokenServiceImpl(List<TokenGeneratorStrategy> strategies, TokenRepository tokenRepository) {
        this.strategies = strategies;
        this.tokenRepository = tokenRepository;
    }

    @Override
    public Token generateToken(User user, TokenType tokenType) {
        Token token = new Token(user, generateTokenValueForType(tokenType), tokenType);

        return tokenRepository.save(token);
    }

    @Override
    public Token generateTokenWithHoursExpired(User user, TokenType tokenType, Integer hours) {
        Token token = new Token(user, generateTokenValueForType(tokenType),tokenType,LocalDateTime.now().plusHours(hours));

        return tokenRepository.save(token);
    }

    @Override
    public Token getLastUserTokenByType(Integer userId, TokenType tokenType) {
        return Optional.ofNullable(tokenRepository.findLastUserTokenByTokenType(userId,tokenType))
                .orElseThrow(() -> new NotFoundException("Token not found!"));
    }

    @Override
    public void delete(Token token) {
        tokenRepository.delete(token);
    }

    private String generateTokenValueForType(TokenType tokenType){

        return findStrategy(tokenType).generateTokenValue();
    }

    private TokenGeneratorStrategy findStrategy(TokenType tokenType){
        return strategies.stream()
                .filter(strategy -> strategy.supports(tokenType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No strategy found for this token type"));
    }


}
