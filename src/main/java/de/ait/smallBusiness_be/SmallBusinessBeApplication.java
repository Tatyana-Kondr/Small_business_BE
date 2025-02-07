package de.ait.smallBusiness_be;

import de.ait.smallBusiness_be.products.model.Dimensions;
import de.ait.smallBusiness_be.products.model.Product;
import de.ait.smallBusiness_be.products.model.ProductCategory;
import de.ait.smallBusiness_be.products.model.UnitOfMeasurement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootApplication
public class SmallBusinessBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmallBusinessBeApplication.class, args);
    }
}
