package com.kapia.registration;

import com.kapia.users.ApplicationUser;
import com.kapia.users.ApplicationUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

    private final ApplicationUserRepository applicationUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public RegistrationService(ApplicationUserRepository applicationUserRepository, PasswordEncoder passwordEncoder) {
        this.applicationUserRepository = applicationUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(RegistrationRequest request) {

        if (!validateRole(request.authority())) {
            throw new IllegalStateException("Role can only be ROLE_ADMIN");
        }
        if (!checkLimitOfUsers()) {
            throw new IllegalStateException("Limit of users reached");
        }
        if (checkIfUserExists(request.username())) {
            throw new IllegalStateException("Username is not available");
        }

        var user = new ApplicationUser();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setAuthority(request.authority());
        applicationUserRepository.save(user);
    }

    private boolean validateRole(String role) {
        return role.equals("ROLE_ADMIN");
    }

    private boolean checkLimitOfUsers() {
        return applicationUserRepository.count() < 10;
    }

    private boolean checkIfUserExists(String username) {
        return applicationUserRepository.findByUsername(username).isPresent();
    }

}
