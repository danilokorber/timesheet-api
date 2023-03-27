package io.easyware.timesheet.utils.healthChecker.health;

import io.easyware.timesheet.utils.healthChecker.shared.HealthHelper;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;

@Liveness
public class ApplicationSettingsHealthCheck implements HealthCheck {

    final String HEALTH_CHECK_NAME = "Application settings";
    HealthCheckResponseBuilder hcrb = HealthHelper.buildNew(HEALTH_CHECK_NAME);

    @ConfigProperty(name="quarkus.swagger-ui.path")
    String swaggerUiPath;

    @ConfigProperty(name="quarkus.swagger-ui.always-include")
    boolean swaggerUiAlwaysIncludes;

    @ConfigProperty(name="quarkus.log.level")
    boolean logLevel;

    @Override
    public HealthCheckResponse call() {

        HealthHelper.addData(hcrb, "swagger-ui.path", swaggerUiPath);
        HealthHelper.addData(hcrb, "swagger-ui.enabled", swaggerUiAlwaysIncludes);
        HealthHelper.addData(hcrb, "quarkus.log-level", logLevel);

        return hcrb.up().build();
    }
}
