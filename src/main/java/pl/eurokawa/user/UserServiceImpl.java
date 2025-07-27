package pl.eurokawa.user;

import java.util.List;
import java.util.Optional;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.router.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
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
    public User registerUser(RegisterUserRequest request){
        String securedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                securedPassword
        );

        return userRepository.save(user);
    }

    public void setUserNewPassword (String email, String password){
        User userByEmail = getByEmail(email);

        if (getAll().contains(userByEmail)){
            userByEmail.setPassword(passwordEncoder.encode(password));
            save(userByEmail);

            Notification notification = Notification.show("Poprawnie zmieniono hasło",3000, Notification.Position.BOTTOM_CENTER);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        }
        else {
            Notification notification = Notification.show("Taki użytkownik nie jest zarejestrowany!",3000, Notification.Position.BOTTOM_CENTER);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    //koncze na tym ze mam dto dla passwordUserRequest ale musze je wdrozyc
    //Ave Maryja!
    @Override
    public void updateUserPassword(User user, PasswordUserRequest request){
        user.setPassword(request.getPassword());
        userRepository.save(user);
    }

    public User getByEmail(String email){

        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> new NotFoundException("Cannot find user by email: " + email));
    }

    @Override
    public User getById(Integer userId){

        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Cannot find user by id:  " + userId));
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User save(User entity) {
        return userRepository.save(entity);
    }

    public void delete(Integer userId) {
        userRepository.deleteById(userId);
    }

    public Page<User> list(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Page<User> list(Pageable pageable, Specification<User> filter) {
        return userRepository.findAll(filter, pageable);
    }

    public int count() {
        return (int) userRepository.count();
    }

}
