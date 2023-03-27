package io.easyware.timesheet.utils.healthChecker.health;

import io.easyware.timesheet.utils.healthChecker.shared.HealthHelper;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;

@Liveness
public class VersionHealthCheck implements HealthCheck {
    final String HEALTH_CHECK_NAME = "Application version";
    HealthCheckResponseBuilder hcrb = HealthHelper.buildNew(HEALTH_CHECK_NAME);

    @ConfigProperty(name="quarkus.application.version")
    String version;

    @Override
    public HealthCheckResponse call() {
        HealthHelper.addData(hcrb, "version", version);
        return hcrb.up().build();
    }
}
