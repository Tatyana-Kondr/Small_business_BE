package de.ait.smallBusiness_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class SmallBusinessBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmallBusinessBeApplication.class, args);
    }
}
