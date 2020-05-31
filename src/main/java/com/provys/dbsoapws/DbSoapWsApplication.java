package com.provys.dbsoapws;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Spring application class - configures bean lookup rules.
 */
@SpringBootApplication(scanBasePackages = "com.provys")
@ConfigurationPropertiesScan(basePackages = "com.provys")
public class DbSoapWsApplication {

  /**
   * Main method, executed to start Spring Boot application.
   *
   * @param args are command line arguments
   */
  public static void main(String[] args) {
    //noinspection resource
    SpringApplication.run(DbSoapWsApplication.class, args);
  }
}
