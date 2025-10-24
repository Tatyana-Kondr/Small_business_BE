package de.ait.smallBusiness_be.configuration;


import de.ait.smallBusiness_be.customers.dto.CustomerDto;
import de.ait.smallBusiness_be.customers.dto.NewCustomerDto;
import de.ait.smallBusiness_be.customers.model.Customer;
import de.ait.smallBusiness_be.payments.dto.PaymentDto;
import de.ait.smallBusiness_be.payments.dto.PaymentPrefillDto;
import de.ait.smallBusiness_be.payments.model.Payment;
import de.ait.smallBusiness_be.productions.dto.ProductionDto;
import de.ait.smallBusiness_be.productions.dto.ProductionItemDto;
import de.ait.smallBusiness_be.productions.model.Production;
import de.ait.smallBusiness_be.productions.model.ProductionItem;
import de.ait.smallBusiness_be.products.dto.ProductDto;
import de.ait.smallBusiness_be.products.model.Product;
import de.ait.smallBusiness_be.purchases.dto.PurchaseDto;
import de.ait.smallBusiness_be.purchases.dto.PurchaseItemDto;
import de.ait.smallBusiness_be.purchases.model.Purchase;
import de.ait.smallBusiness_be.purchases.model.PurchaseItem;
import de.ait.smallBusiness_be.sales.dto.NewShippingDimensionsDto;
import de.ait.smallBusiness_be.sales.dto.SaleDto;
import de.ait.smallBusiness_be.sales.dto.SaleItemDto;
import de.ait.smallBusiness_be.sales.models.Sale;
import de.ait.smallBusiness_be.sales.models.SaleItem;
import de.ait.smallBusiness_be.sales.models.ShippingDimensions;
import de.ait.smallBusiness_be.users.dto.NewUserDto;
import de.ait.smallBusiness_be.users.dto.UserDto;
import de.ait.smallBusiness_be.users.model.User;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * SmallBusiness_BE
 * 31.10.2024
 *
 * @author Kondratyeva
 */

@Configuration
public class ServiceConfiguration {

    @Bean
    ModelMapper getModelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE)
                .setMatchingStrategy(MatchingStrategies.STRICT);

        //  Customer ↔ CustomerDto
        modelMapper.createTypeMap(Customer.class, CustomerDto.class)
                .addMapping(Customer::getAddress, CustomerDto::setAddress);

        modelMapper.createTypeMap(CustomerDto.class, Customer.class)
                .addMapping(CustomerDto::getAddress, Customer::setAddress);

        modelMapper.createTypeMap(NewCustomerDto.class, Customer.class)
                .addMapping(NewCustomerDto::getAddressDto, Customer::setAddress);

        // PurchaseItem ↔ PurchaseItemDto
        modelMapper.createTypeMap(PurchaseItem.class, PurchaseItemDto.class)
                .addMapping(src -> src.getPurchase().getId(), PurchaseItemDto::setPurchaseId)
                .addMapping(src -> src.getProduct().getId(), PurchaseItemDto::setProductId)
                .addMapping(src -> src.getProduct().getArticle(), PurchaseItemDto::setProductArticle);

        //  Purchase ↔ PurchaseDto
        modelMapper.createTypeMap(Purchase.class, PurchaseDto.class)
                .addMapping(src -> src.getVendor().getId(), PurchaseDto::setVendorId)
                .addMapping(src -> src.getVendor().getName(), PurchaseDto::setVendorName);

        //  SaleItem ↔ SaleItemDto
        modelMapper.createTypeMap(SaleItem.class, SaleItemDto.class)
                .addMapping(src -> src.getSale().getId(), SaleItemDto::setSaleId)
                .addMapping(src -> src.getProduct().getId(), SaleItemDto::setProductId)
                .addMapping(src -> src.getProduct().getArticle(), SaleItemDto::setProductArticle);

        //  Sale ↔ SaleDto
        modelMapper.createTypeMap(Sale.class, SaleDto.class)
                .addMapping(src -> src.getCustomer().getId(), SaleDto::setCustomerId)
                .addMapping(src -> src.getCustomer().getName(), SaleDto::setCustomerName)
                .addMapping(src -> src.getShipping().getId(), SaleDto::setShippingId);

        //  ShippingDimensions ↔ NewShippingDimensionsDto
        modelMapper.createTypeMap(ShippingDimensions.class, NewShippingDimensionsDto.class)
                .addMappings(mapper -> {
                    mapper.map(ShippingDimensions::getWidth, NewShippingDimensionsDto::setWidth);
                    mapper.map(ShippingDimensions::getHeight, NewShippingDimensionsDto::setHeight);
                    mapper.map(ShippingDimensions::getLength, NewShippingDimensionsDto::setLength);
                    mapper.map(ShippingDimensions::getWeight, NewShippingDimensionsDto::setWeight);
                });

        //  Payment ↔ PaymentDto
        modelMapper.createTypeMap(Payment.class, PaymentDto.class)
                .addMapping(src -> src.getCustomer().getId(), PaymentDto::setCustomerId)
                .addMapping(src -> src.getCustomer().getName(), PaymentDto::setCustomerName)
                .addMapping(src -> src.getSale().getId(), PaymentDto::setSaleId)
                .addMapping(src -> src.getPurchase().getId(), PaymentDto::setPurchaseId)
                .addMapping(src -> src.getPaymentMethod().getId(), PaymentDto::setPaymentMethodId)
                .addMapping(src -> src.getPaymentProcess().getId(), PaymentDto::setPaymentProcessId);

        modelMapper.createTypeMap(Payment.class, PaymentPrefillDto.class)
                .addMapping(src -> src.getDocument().getId(), PaymentPrefillDto::setDocumentId)
                .addMapping(src -> src.getDocument().getName(), PaymentPrefillDto::setDocumentName)
                .addMapping(src -> src.getCustomer().getId(), PaymentPrefillDto::setCustomerId)
                .addMapping(src -> src.getCustomer().getName(), PaymentPrefillDto::setCustomerName);


        //  ProductionItem ↔ ProductionItemDto
        modelMapper.createTypeMap(ProductionItem.class, ProductionItemDto.class)
                .addMapping(src -> src.getProduction().getId(), ProductionItemDto::setProductionId)
                .addMapping(src -> src.getProduct().getId(), ProductionItemDto::setProductId);

        //  Production ↔ ProductionDto
        modelMapper.createTypeMap(Production.class, ProductionDto.class)
                .addMapping(src -> src.getProduct().getId(), ProductionDto::setProductId);

        //  User ↔ UserDto
        modelMapper.createTypeMap(User.class, UserDto.class);
        modelMapper.createTypeMap(NewUserDto.class, User.class);

        //  Product ↔ ProductDto
        modelMapper.createTypeMap(Product.class, ProductDto.class)
                .addMappings(mapper -> {
                    mapper.map(Product::getDimensions, ProductDto::setNewDimensions);
                    mapper.map(Product::getUnitOfMeasurement, ProductDto::setUnitOfMeasurement);
                });

        modelMapper.createTypeMap(ProductDto.class, Product.class)
                .addMappings(mapper -> {
                    mapper.map(ProductDto::getNewDimensions, Product::setDimensions);
                    mapper.map(ProductDto::getUnitOfMeasurement, Product::setUnitOfMeasurement);
                });

        return modelMapper;
    }

    @Bean
    @Profile("dev") // активируется только при dev-профиле
    public WebMvcConfigurer corsDev() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("http://localhost:5173")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                        .allowCredentials(true);
            }
        };
    }

    @Bean
    @Profile("prod") // активируется только при prod-профиле
    public WebMvcConfigurer corsProd() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("http://wolke1:8080", "http://localhost:8080")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowCredentials(true);
            }
        };
    }
}
