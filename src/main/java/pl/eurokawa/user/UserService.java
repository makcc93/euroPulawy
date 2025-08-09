package pl.eurokawa.user;

import pl.eurokawa.user.DTO.PasswordUserRequest;
import pl.eurokawa.user.DTO.RegisterUserRequest;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User registerUser(RegisterUserRequest request, String repeatedPassword);
    void setUserNewPassword (String email, String password);
    void updateUserPassword(User user, PasswordUserRequest request);
    User getById(Integer userId);
    User getByEmail(String email);
    List<User> getAll();
    List<User> findOnlyConfirmedUsers();
    List<User> findByType(UserType userType);
    User save(User entity);
    void delete(Integer userId);
    int count();
    List<User> findAll();
}
