package com.kapia.keys;

import com.kapia.ratelimiting.PricingPlan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/key")
public class KeyController {

    private final KeyService keyService;

    @Autowired
    public KeyController(KeyService keyService) {
        this.keyService = keyService;
    }

    @GetMapping
    public String getKey(@RequestParam String plan) {
        return keyService.generateKeyAndAddToRedis(PricingPlan.valueOf(plan));
    }


}
