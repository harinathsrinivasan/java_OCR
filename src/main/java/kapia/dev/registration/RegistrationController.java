package kapia.dev.registration;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Hidden
public class RegistrationController {

    private final RegistrationService registrationService;

    @Autowired
    public RegistrationController(RegistrationService registrationService) throws IllegalStateException {
        this.registrationService = registrationService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegistrationRequest request) {
        registrationService.register(request);
        return ResponseEntity.status(201).body("User registered successfully");
    }

}
