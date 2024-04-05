package com.kapia.registration;

import com.kapia.users.ApplicationUserRepository;
import org.junit.After;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RegistrationController.class, RegistrationService.class})
public class TestRegistrationController {

    @MockBean
    private ApplicationUserRepository applicationUserRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Mock
    private RegistrationService registrationService;

    @InjectMocks
    private RegistrationController registrationController;

    private final String VALID_USERNAME = "username";
    private final String VALID_PASSWORD = "password";
    private final String VALID_ROLE = "ROLE_ADMIN";

    @After
    public void tearDown() {
        reset(registrationService);
    }

    @Test
    public void givenRegistrationRequest_whenRegister_thenUserRegisteredSuccessfully() {

        RegistrationRequest request = new RegistrationRequest(VALID_USERNAME, VALID_PASSWORD, VALID_ROLE);

        when(registrationService.register(request)).thenReturn(VALID_USERNAME);

        ResponseEntity<String> response = registrationController.register(request);

        Assertions.assertEquals(201, response.getStatusCode().value());
        Assertions.assertEquals("User username created", response.getBody());

        verify(registrationService, times(1)).register(request);
    }

    @Test
    public void givenRegistrationRequestWithNoUsername_whenRegister_thenUserNotRegistered() {
        RegistrationRequest request = new RegistrationRequest("", VALID_PASSWORD, VALID_ROLE);

        willThrow(new IllegalStateException("Invalid request")).given(registrationService).register(request);

        Assert.assertThrows(IllegalStateException.class, () -> registrationController.register(request));

        verify(registrationService, times(1)).register(request);
    }

    @Test
    public void givenLimitOfUsersReached_whenRegister_thenUserNotRegistered() {
        RegistrationRequest request = new RegistrationRequest(VALID_USERNAME, VALID_PASSWORD, VALID_ROLE);

        willThrow(new IllegalStateException("Limit of users reached")).given(registrationService).register(request);

        Assertions.assertThrows(IllegalStateException.class, () -> registrationController.register(request));

        verify(registrationService, times(1)).register(request);
    }

    @Test
    public void givenUsernameNotAvailable_whenRegister_thenUserNotRegistered() {
        RegistrationRequest request = new RegistrationRequest(VALID_USERNAME, VALID_PASSWORD, VALID_ROLE);

        willThrow(new IllegalStateException("Username is not available")).given(registrationService).register(request);

        Assertions.assertThrows(IllegalStateException.class, () -> registrationController.register(request));

        verify(registrationService, times(1)).register(request);
    }

}
