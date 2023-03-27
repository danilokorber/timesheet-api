package io.easyware.timesheet.utils.healthChecker.shared;

import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * The HealthHelper class provides utility functions to build and manipulate health check responses.
 */
@Log
public abstract class HealthHelper {
    /**
     * A list of property names to mask when added to a HealthCheckResponseBuilder
     */
    static String[] safeProperties = ConfigProvider.getConfig().getValue("health.safe-properties", String.class).split(",");
    //Cannot use @ConfigMapping because it needs to be static

    static List<String> safePropertiesList = new ArrayList<>(Arrays.asList(safeProperties));

    static final List<String> DEFAULT_PROPERTIES_TO_MASK = Arrays.asList("password", "credential", "token", "secret");


    /**
     * Returns a new HealthCheckResponseBuilder with the given name.
     *
     * @param name the name of the health check
     * @return a new HealthCheckResponseBuilder
     */
    public static HealthCheckResponseBuilder buildNew(String name) {
        return HealthCheckResponse.builder().name(name);
    }

    /**
     * Adds all properties of the given object to the HealthCheckResponseBuilder.
     *
     * @param obj  the object whose properties to add
     * @param hcrb the HealthCheckResponseBuilder to add properties to
     */
    public static void addPropertiesToHcrb(Object obj, HealthCheckResponseBuilder hcrb) {
        addPropertiesToHcrb(obj, hcrb, "");
    }

    /**
     * Adds all properties of the given object to the HealthCheckResponseBuilder with
     * the given prefix.
     *
     * @param obj    the object whose properties to add
     * @param hcrb   the HealthCheckResponseBuilder to add properties to
     * @param prefix the prefix to prepend to each property name
     */
    public static void addPropertiesToHcrb(Object obj, HealthCheckResponseBuilder hcrb, String prefix) {
        if (!prefix.isEmpty()) prefix += "-";

        for (Method method : obj.getClass().getDeclaredMethods()) {
            String propertyName = prefix + method.getName();

            if (!propertyName.contains("fillInOptionals")) {
                try {
                    Object propertyValue = method.invoke(obj);
                    addData(hcrb, propertyName, propertyValue);
                } catch (Exception e) {
                    log.severe("  " + propertyName + " failed: " + e.getClass().getName());
                }
            }
        }
    }

    /**
     * Adds a data key-value pair to the HealthCheckResponseBuilder. If the key
     * contains a safe property name, the value is masked.
     *
     * @param hcrb  the HealthCheckResponseBuilder to add the data to
     * @param key   the key of the data
     * @param value the value of the data
     */
    public static void addData(HealthCheckResponseBuilder hcrb, String key, Object value) {
        String finalValue;
        if (value instanceof Optional) {
            finalValue = ((Optional<?>) value).isEmpty() ? "" : ((Optional<?>) value).get().toString();
        } else {
            finalValue = value.toString();
        }

        // Add default properties if necessary
        DEFAULT_PROPERTIES_TO_MASK.forEach(property -> {
            if (safePropertiesList.stream().noneMatch(property::equalsIgnoreCase)) {
                safePropertiesList.add(property);
            }
        }) ;

        // Mask safe properties
        if (safePropertiesList.stream().anyMatch(sp -> key.toLowerCase().contains(sp))) {
            finalValue = maskString(finalValue);
        }

        finalValue = finalValue.isEmpty() ? null : finalValue;

        hcrb.withData(key, finalValue);
    }

    /**
     * Masks the given string by replacing all characters except the first and last two with asterisks.
     *
     * @param clearText the string to mask
     * @return the masked string
     */
    private static String maskString(String clearText) {
        log.info("Masking " + clearText);
        String maskedText = clearText.isEmpty() ? "" : StringUtils.overlay(clearText, "*********************", 2, clearText.length() - 2);
        log.info("result:  " + clearText);
        return maskedText;
    }
}
