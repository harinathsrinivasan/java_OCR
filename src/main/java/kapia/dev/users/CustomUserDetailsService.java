package kapia.dev.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final ApplicationUserRepository applicationUserRepository;

    @Autowired
    public CustomUserDetailsService(ApplicationUserRepository applicationUserRepository) {
        this.applicationUserRepository = applicationUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return applicationUserRepository.findByUsername(username)
                .map(ApplicationUserAdapter::new)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found"));
    }

    public void createSuperUser(String username, String password) {
        if (!applicationUserRepository.findByUsername(username).isPresent()) {

            PasswordEncoder encoder = new BCryptPasswordEncoder();

            var user = new ApplicationUser();
            user.setUsername(username);
            user.setPassword(encoder.encode(password));
            user.setAuthority("ROLE_SUPERUSER");
            applicationUserRepository.save(user);
        }
    }
}
