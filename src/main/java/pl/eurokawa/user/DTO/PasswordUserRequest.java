package pl.eurokawa.user.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordUserRequest {
    private String password;

    public PasswordUserRequest(String password){
        this.password = password;
    }
}
