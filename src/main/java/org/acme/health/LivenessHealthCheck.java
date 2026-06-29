package org.acme.health;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.acme.service.TransactionService;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

@Liveness
@Singleton
public class LivenessHealthCheck implements HealthCheck {

    @Inject
    TransactionService transactionService;

    @Override
    public HealthCheckResponse call() {

        return HealthCheckResponse.up("Custom health check");
    }
}
