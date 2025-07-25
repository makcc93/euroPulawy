package pl.eurokawa.token;

import pl.eurokawa.user.User;

public interface TokenService {
    Token generateToken(User user,TokenType tokenType);
    Token generateTokenWithHoursExpired(User user,TokenType tokenType,Integer hours);
    Token getLastUserTokenByType(Integer userId, TokenType tokenType);
    void delete(Token token);
}
