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
        Product product = new Product(1L, "product", "EL-6", "qwer", BigDecimal.valueOf(10.10), BigDecimal.valueOf(15.10), UnitOfMeasurement.KG, BigDecimal.valueOf(5.0), new Dimensions(BigDecimal.valueOf(5.000), BigDecimal.valueOf(5.0), BigDecimal.valueOf(5.0)),new ProductCategory(1, "COMPUTER & ZUBEHÖR","COM"),"drtfhdsfhd","345", LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now());
        System.out.println(product);
    }
}
