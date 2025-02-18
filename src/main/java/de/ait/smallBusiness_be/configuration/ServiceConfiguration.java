package de.ait.smallBusiness_be.configuration;


import de.ait.smallBusiness_be.customers.dto.CustomerDto;
import de.ait.smallBusiness_be.customers.dto.NewCustomerDto;
import de.ait.smallBusiness_be.customers.model.Customer;
import de.ait.smallBusiness_be.productions.dto.ProductionDto;
import de.ait.smallBusiness_be.productions.dto.ProductionItemDto;
import de.ait.smallBusiness_be.productions.model.Production;
import de.ait.smallBusiness_be.productions.model.ProductionItem;
import de.ait.smallBusiness_be.purchases.dto.PurchaseDto;
import de.ait.smallBusiness_be.purchases.dto.PurchaseItemDto;
import de.ait.smallBusiness_be.purchases.model.Purchase;
import de.ait.smallBusiness_be.purchases.model.PurchaseItem;
import de.ait.smallBusiness_be.sales.dto.SaleDto;
import de.ait.smallBusiness_be.sales.dto.SaleItemDto;
import de.ait.smallBusiness_be.sales.models.Sale;
import de.ait.smallBusiness_be.sales.models.SaleItem;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
        modelMapper.getConfiguration().setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE)
                .setMatchingStrategy(MatchingStrategies.STRICT);

        // Настройка маппинга Customer -> CustomerDto
        modelMapper.createTypeMap(Customer.class, CustomerDto.class)
                .addMapping(Customer::getAddress, CustomerDto::setAddress);

        // Настройка маппинга CustomerDto -> Customer
        modelMapper.createTypeMap(CustomerDto.class, Customer.class)
                .addMapping(CustomerDto::getAddress, Customer::setAddress);

        // Настройка маппинга NewCustomerDto -> Customer
        modelMapper.createTypeMap(NewCustomerDto.class, Customer.class)
                .addMapping(NewCustomerDto::getAddressDto, Customer::setAddress);

        //  Добавляем маппинг для PurchaseItem -> PurchaseItemDto
        modelMapper.createTypeMap(PurchaseItem.class, PurchaseItemDto.class)
                .addMapping(src -> src.getPurchase().getId(), PurchaseItemDto::setPurchaseId);

        //  Добавляем маппинг для Purchase -> PurchaseDto
        modelMapper.createTypeMap(Purchase.class, PurchaseDto.class)
                .addMapping(src -> src.getVendor().getId(), PurchaseDto::setVendorId);

        //  Добавляем маппинг для ProductionItem -> ProductionItemDto
        modelMapper.createTypeMap(ProductionItem.class, ProductionItemDto.class)
                .addMapping(src -> src.getProduction().getId(), ProductionItemDto::setProductionId);

        modelMapper.typeMap(Production.class, ProductionDto.class)
                .addMappings(mapper -> mapper.map(src -> src.getProduct().getId(), ProductionDto::setProductId));

        modelMapper.typeMap(ProductionItem.class, ProductionItemDto.class)
                .addMappings(mapper -> mapper.map(src -> src.getProduct().getId(), ProductionItemDto::setProductId));
        return modelMapper;

        //  Добавляем маппинг для SaleItem -> SaleItemDto
        //modelMapper.createTypeMap(SaleItem.class, SaleItemDto.class)
        //        .addMapping(src -> src.getSale().getId(), SaleItemDto::);

        //  Добавляем маппинг для Sale -> SaleDto
        //modelMapper.createTypeMap(Sale.class, SaleDto.class)
        //        .addMapping(src -> src.getCustomer().getId(), SaleDto::setCustomerId);
    }

    @Bean
    public WebMvcConfigurer cors() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**").allowedOrigins("*");
            }
        };
    }
}
