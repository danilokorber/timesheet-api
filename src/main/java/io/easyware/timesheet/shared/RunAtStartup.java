package io.easyware.timesheet.shared;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import lombok.extern.java.Log;
import org.eclipse.microprofile.config.ConfigProvider;

@QuarkusMain
public class RunAtStartup {

    public static void main(String ... args) {
        Quarkus.run(PrintSettings.class, args);
    }

    @Log
    public static class PrintSettings implements QuarkusApplication {

        @Override
        public int run(String... args) throws Exception {
            System.out.println(" ");
            System.out.println(banner("QUARKUS"));
            System.out.println("REST path    : " + ConfigProvider.getConfig().getValue("quarkus.resteasy-reactive.path", String.class));
            System.out.println("Swagger      : " + ConfigProvider.getConfig().getValue("quarkus.swagger-ui.path", String.class));
            System.out.println("Log level    : " + ConfigProvider.getConfig().getValue("quarkus.log.level", String.class));

            System.out.println(" ");
            System.out.println(banner("JDBC"));
            System.out.println("Database     : " + ConfigProvider.getConfig().getValue("quarkus.datasource.db-kind", String.class));
            System.out.println("URL          : " + ConfigProvider.getConfig().getValue("quarkus.datasource.jdbc.url", String.class));
            System.out.println("User         : " + ConfigProvider.getConfig().getValue("quarkus.datasource.username", String.class));
            System.out.println("Password     : " + showAsPassword(ConfigProvider.getConfig().getValue("quarkus.datasource.password", String.class)));
            System.out.println("Flyway       : " + ConfigProvider.getConfig().getValue("quarkus.flyway.migrate-at-start", String.class));
            System.out.println("Flyway files : " + ConfigProvider.getConfig().getValue("quarkus.flyway.locations", String.class));

            Quarkus.waitForExit();
            return 0;
        }

        private String showAsPassword(String clearText) {
            String password = "";
            for (int i = 0; i < clearText.length(); i++) {
                if (i > 1 && i < clearText.length() - 2)
                    password += "*";
                else
                    password += clearText.substring(i,i+1);
            }
            return password;
        }

        private String banner(String text) {
            String b = "##  " + text + "  ################################################################################";
            return b.substring(0,80);
        }
    }
}

