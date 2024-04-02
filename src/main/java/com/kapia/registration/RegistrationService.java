package com.kapia.registration;

import com.kapia.users.ApplicationUser;
import com.kapia.users.ApplicationUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationService.class);

    @Value("${admin.accounts.limit:10}")
    private int limitOfUsers;

    @Value("${admin.role.name:ROLE_ADMIN}")
    private String ROLE_ADMIN;

    private final ApplicationUserRepository applicationUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public RegistrationService(ApplicationUserRepository applicationUserRepository, PasswordEncoder passwordEncoder) {
        this.applicationUserRepository = applicationUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(RegistrationRequest request) {

        if (!isRequestValid(request)) {
            throw new IllegalStateException("Invalid request");
        }
        if (!checkLimitOfUsers()) {
            throw new IllegalStateException("Limit of users reached");
        }
        if (checkIfUserExists(request.username())) {
            throw new IllegalStateException("Username is not available");
        }

        LOGGER.debug("Creating user: " + request.username());

        var user = new ApplicationUser();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setAuthority(request.authority());
        applicationUserRepository.save(user);
    }

    private boolean isRequestValid(RegistrationRequest request) {

        if (request.username() == null || request.username().isEmpty()) {
            LOGGER.debug("Invalid username");
            return false;
        }
        if (request.password() == null || request.password().isEmpty()) {
            LOGGER.debug("Invalid password");
            return false;
        }
        if (request.authority() == null || !request.authority().equals(ROLE_ADMIN)) {
            LOGGER.debug("Invalid authority");
            return false;
        }

        return true;
    }

    private boolean checkLimitOfUsers() {
        LOGGER.debug("Users count: " + applicationUserRepository.countByAuthority(ROLE_ADMIN) + " Limit: " + limitOfUsers);
        return applicationUserRepository.countByAuthority(ROLE_ADMIN) < limitOfUsers;
    }

    private boolean checkIfUserExists(String username) {
        LOGGER.debug("Checking if user exists: " + username);
        return applicationUserRepository.findByUsername(username).isPresent();
    }

}
