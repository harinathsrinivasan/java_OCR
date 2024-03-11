package kapia.dev.ratelimiting;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;

import java.time.Duration;

public enum PricingPlan {

    FREE {
        @Override
        Bandwidth getLimit() {
            return Bandwidth.classic(1, Refill.intervally(1, Duration.ofHours(1)));
        }
    },
    BASIC {
        @Override
        Bandwidth getLimit() {
            return Bandwidth.classic(2, Refill.intervally(2, Duration.ofHours(1)));
        }
    },
    PRO {
        @Override
        Bandwidth getLimit() {
            return Bandwidth.classic(3, Refill.intervally(3, Duration.ofHours(1)));
        }
    };

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

    abstract Bandwidth getLimit();
}
