package com.profinch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
/*
 * public class BulkuploadApplication {
 * 
 * public static void main(String[] args) {
 * SpringApplication.run(BulkuploadApplication.class, args); }
 * 
 * }
 */

public class BulkuploadApplication extends SpringBootServletInitializer {  // Extending SpringBootServletInitializer
    public static void main(String[] args) {
        SpringApplication.run(BulkuploadApplication.class, args);
    }
}
