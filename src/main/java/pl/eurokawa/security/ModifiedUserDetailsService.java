package pl.eurokawa.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import pl.eurokawa.user.User;
import pl.eurokawa.user.UserRepository;
@Primary
@Component
public class ModifiedUserDetailsService implements UserDetailsService {
    private static final Logger log = LogManager.getLogger(ModifiedUserDetailsService.class);
    private final UserRepository userRepository;

    public ModifiedUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Użytkownik nie został znaleziony"));

        return new ModifiedUserDetails(user);
    }
}
