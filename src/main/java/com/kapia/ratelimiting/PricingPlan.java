package com.kapia.ratelimiting;

import io.github.bucket4j.Bandwidth;
import org.springframework.core.env.Environment;

import java.time.Duration;

public enum PricingPlan {

    FREE {
        @Override
        Bandwidth getLimit(Environment environment) {
            return getBandwidth(environment, "free");
        }
    },
    BASIC {
        @Override
        Bandwidth getLimit(Environment environment) {
            return getBandwidth(environment, "basic");
        }
    },
    PRO {
        @Override
        Bandwidth getLimit(Environment environment) {
            return getBandwidth(environment, "pro");
        }
    };

    static private Bandwidth getBandwidth(Environment environment, String plan) {
        int capacity = environment.getProperty("pricing.plans." + plan + ".limit.capacity", Integer.class, 1);
        int refillTokens = environment.getProperty("pricing.plans." + plan + ".limit.tokens", Integer.class, 1);
        int refillDuration = environment.getProperty("pricing.plans.refill.rate.in.minutes", Integer.class, 1);

        return Bandwidth.builder()
                .capacity(capacity)
                .refillIntervally(refillTokens, Duration.ofMinutes(refillDuration))
                .build();
    }

    static PricingPlan resolvePlanFromKey(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            return FREE;
        }
        if (apiKey.startsWith("PX")) {
            return BASIC;
        }
        if (apiKey.startsWith("AX")) {
            return PRO;
        }
        return FREE;
    }

    abstract Bandwidth getLimit(Environment environment);
}