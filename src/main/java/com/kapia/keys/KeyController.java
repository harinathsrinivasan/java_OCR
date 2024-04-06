package com.kapia.keys;

import com.kapia.ratelimiting.PricingPlan;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.slf4j.LoggerFactory.getLogger;

@RestController
@RequestMapping("/key")
@SecurityRequirement(name = "basicAuth")
public class KeyController {

    private final static Logger LOGGER = getLogger(KeyController.class);

    private final KeyService keyService;

    @Autowired
    public KeyController(KeyService keyService) {
        this.keyService = keyService;
    }

    @GetMapping
    @Operation(summary = "Request a API key", description = "Endpoint that generates a new API key.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Key generated", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "text/plain")),
    })
    public String getKey(@Parameter(name = "plan", description = "Pricing plan for the key, can be either BASIC or PRO") @RequestParam String plan) {
        LOGGER.info("Generating key for plan: {}", plan);
        return keyService.generateKeyAndAddToRedis(PricingPlan.valueOf(plan));
    }


}
