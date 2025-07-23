package pl.eurokawa.security;

import com.vaadin.flow.server.VaadinSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.eurokawa.user.User;
import pl.eurokawa.user.UserRepository;
import pl.eurokawa.user.UserService;

@Service
public class SecurityService {

    private final UserRepository userRepository;
    private final UserService userService;

    public SecurityService(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    public boolean loggedUserHasRole(String role){
        Authentication authentication = VaadinSession.getCurrent().getAttribute(Authentication.class);

        if (authentication == null || !authentication.isAuthenticated()){
            return false;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authRole -> authRole.equals("ROLE_" + role));
    }

    public boolean hasAccessToCoffee(User user){
        return user != null && Boolean.TRUE.equals(user.getIsCoffeeMember());
    }

    public String getLoggedUserFirstAndLastName(){
        Authentication authentication = VaadinSession.getCurrent().getAttribute(Authentication.class);

        if (authentication != null || authentication.getPrincipal() instanceof ModifiedUserDetails){
            ModifiedUserDetails user = (ModifiedUserDetails) authentication.getPrincipal();

            String email = user.getUsername();
            User userByEmail = userRepository.findUserByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Nie mogę znaleźć zalogowanego użytkownika"));

            String firstName = userByEmail.getFirstName();
            String lastName = userByEmail.getLastName();

            return firstName + " " + lastName;
        }
        else {

            return "";
        }

    }

    public User getLoggedUser(){
        Authentication authentication = VaadinSession.getCurrent().getAttribute(Authentication.class);
        String userEmail = authentication.getName();

        return userService.getUserByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Nie rozpoznano zalogowanego użytkownika!"));
    }

//    public User getLoggedUser(){
//        Authentication authentication = VaadinSession.getCurrent().getAttribute(Authentication.class);
//
//        if (authentication != null || authentication.getPrincipal() instanceof ModifiedUserDetails){
//            ModifiedUserDetails user = (ModifiedUserDetails) authentication.getPrincipal();
//
//            String email = user.getUsername();
//            User userByEmail = userRepository.findUserByEmail(email)
//                    .orElseThrow(() -> new RuntimeException("Nie mogę znaleźć zalogowanego użytkownika"));
//
//            return userByEmail;
//        }
//
//        return new User();
//
//    }
}
