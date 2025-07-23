package pl.eurokawa.security;

import org.springframework.stereotype.Component;

@Component
public class PasswordValidator {
    private static final int PASSWORD_LENGTH_REQUIRED = 6;

    public boolean validatePassword(String password){
        return password.length() >= PASSWORD_LENGTH_REQUIRED;
    }

    public boolean doPasswordsMatch(String password, String repeatedPassword){
        return password.equals(repeatedPassword);
    };

    public static int getPasswordLengthRequired(){
        return PASSWORD_LENGTH_REQUIRED;
    }
}
