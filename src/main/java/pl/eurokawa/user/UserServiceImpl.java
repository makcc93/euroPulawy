package pl.eurokawa.user;

import java.util.List;
import java.util.Optional;

import com.vaadin.flow.router.NotFoundException;
import jakarta.validation.ValidationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.eurokawa.exception.ArgumentNullChecker;
import pl.eurokawa.user.DTO.PasswordUserRequest;
import pl.eurokawa.user.DTO.RegisterUserRequest;

@Service
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User registerUser(RegisterUserRequest request, String repeatedPassword){
        if (!request.password().equals(repeatedPassword)){
            throw new ValidationException("Hasła się nie zgadzają");
        }

        if (userRepository.existsByEmail(request.email())){
            throw new ValidationException("Email już istnieje w bazie użytkowników");
        }

        User user = new User(
                request.firstName(),
                request.lastName(),
                request.email(),
                passwordEncoder.encode(request.password())
        );

        return userRepository.save(user);
    }

    @Override
    public void setUserNewPassword (String email, String password){
        User userByEmail = getByEmail(email);

        if (getAll().contains(userByEmail)){
            userByEmail.setPassword(passwordEncoder.encode(password));
            save(userByEmail);
        }
    }

    @Override
    public void updateUserPassword(User user, PasswordUserRequest request){
        user.setPassword(request.password());
        userRepository.save(user);
    }

    @Override
    public User getById(Integer userId) {

        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Cannot find user by id:  " + userId));
    }
    @Override
    public User getByEmail(String email){
    ArgumentNullChecker.check(email,"Email");

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Cannot find user by email: " + email));
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    public List<User> findOnlyConfirmedUsers() {
        return userRepository.findOnlyConfirmedUsers();
    }

    @Override
    public List<User> findByType(UserType userType) {
        ArgumentNullChecker.check(userType,"User type");

        return Optional.ofNullable(userRepository.findByType(userType))
                .orElseThrow(() -> new IllegalArgumentException("Cannot find user by type: " + userType));
    }

    @Override
    public User save(User entity) {
        return userRepository.save(entity);
    }

    @Override
    public void delete(Integer userId) {
        userRepository.deleteById(userId);
    }

    @Override
    public int count() {
        return (int) userRepository.count();
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

}
