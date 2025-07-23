package pl.eurokawa.token;

import org.springframework.stereotype.Service;
import pl.eurokawa.user.User;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
public class TokenService {

    public Token generateToken(User user, TokenType type){
        String value;

        if (type == TokenType.REGISTRATION || type == TokenType.ACCOUNT_CONFIRMATION){
            value = randomChars();
        }
        else if (type == TokenType.PASSWORD_RESET || type == TokenType.SIX_NUMBERS){
            value = randomInt(6);
        }
        else {
            throw new IllegalArgumentException("Unknown token type");
        };

        return new Token(user,value,type, LocalDateTime.now().plusHours(24));
    }

    private String randomInt(int tokenLength){
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < tokenLength ; i++){
            sb.append(random.nextInt(10));
        }

        return sb.toString();
    }

    private String randomChars(){
        return UUID.randomUUID().toString();
    }
}
