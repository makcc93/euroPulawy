package pl.eurokawa.user;

import pl.eurokawa.user.DTO.PasswordUserRequest;
import pl.eurokawa.user.DTO.RegisterUserRequest;

import java.util.Optional;

public interface UserService {
    User registerUser(RegisterUserRequest request);
    void setUserNewPassword (String email, String password);
    void updateUserPassword(User user, PasswordUserRequest request);
    User getUserById(Integer userId);
    User getByEmail(String email);
}
