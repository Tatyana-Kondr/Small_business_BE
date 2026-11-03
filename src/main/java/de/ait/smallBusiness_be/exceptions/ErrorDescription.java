
package de.ait.smallBusiness_be.exceptions;

import lombok.Getter;

@Getter
public enum ErrorDescription {

    // General
    LIST_IS_EMPTY("List is empty"),
    DATA_INTEGRITY_VIOLATION("Data integrity violation"),
    INTERNAL_SERVER_ERROR("Internal server error"),

    // Email
    EMAIL_ALREADY_EXISTS("Email already exists"),
    EMAIL_NOT_EXISTS("Email does not exist"),
    INVALID_EMAIL("Invalid email"),

    // Users and authentication
    USER_NOT_FOUND("User not found"),
    USER_ALREADY_EXISTS("User already exists"),
    USERNAME_ALREADY_EXISTS("Username already exists"),
    INVALID_USERNAME("Invalid username"),
    INVALID_PASSWORD("Invalid password"),
    INVALID_OLD_PASSWORD("Invalid old password"),
    FORBIDDEN("Forbidden"),
    UNAUTHORIZED("Unauthorized"),

    REFRESH_TOKEN_MISSING("Refresh token missing"),
    REFRESH_TOKEN_NOT_FOUND("Refresh token not found"),
    REFRESH_TOKEN_EXPIRED("Refresh token expired"),

    EMPLOYEE_NOT_FOUND("Employee not found"),
    NO_AUTHENTICATED_USER("No authenticated user available for generating JWT"),
    TOKEN_EXPIRED("Token has expired"),
    INVALID_JWT_TOKEN("Invalid JWT token format"),
    JWT_SIGNATURE_ERROR("JWT signature does not match locally computed signature"),
    MISSING_JWT_TOKEN("JWT token is missing"),
    NO_TOKEN_PROVIDED("No token provided"),
    REQUEST_NULL("Request cannot be null"),
    TOKEN_TYPE_NULL("Token type cannot be null"),
    NO_ROLE("No roles found for the user."),
    INVALID_ROLE("Invalid user role"),

    // Company
    COMPANY_NOT_FOUND("Company not found"),
    LOGO_NOT_FOUND("Company logo not found"),

    // Product categories
    CATEGORY_NOT_FOUND("Category not found"),
    CATEGORY_ALREADY_EXISTS("Category already exists"),
    CATEGORY_ARTNAME_ALREADY_EXISTS("Category article name already exists"),
    CATEGORY_DELETE_FAILED("Category cannot be deleted because it is used in other records."),

    // Products
    PRODUCT_NOT_FOUND("Product not found"),
    PRODUCT_ALREADY_EXISTS("Product with the same name and article already exists."),
    LIST_PRODUCTS_IS_EMPTY("List of products is empty"),
    PRODUCT_DELETE_FAILED("Product cannot be deleted because it is used in other records."),

    // Production
    PRODUCTION_AMOUNT("Amount of the product must exceed the cost of materials."),
    PRODUCTION_NOT_FOUND("Production not found"),
    NO_PRODUCT_IN_PRODUCTION("No products in production"),
    PRODUCTION_MATERIALS_REQUIRED("Production must contain at least one material"),

    // Warehouse
    WAREHOUSE_NOT_FOUND("Warehouse record not found for product"),
    WAREHOUSE_INVALID_PRODUCT("Warehouse operation requires a product"),
    WAREHOUSE_INVALID_QUANTITY("Warehouse quantity must be greater than zero"),
    WAREHOUSE_INVALID_DOCUMENT("Invalid warehouse document"),
    WAREHOUSE_INVALID_OPERATION_TYPE("Invalid warehouse operation type"),
    WAREHOUSE_INVALID_ITEM("Invalid warehouse document item"),
    WAREHOUSE_ITEM_READ_FAILED("Failed to read warehouse document item"),
    WAREHOUSE_INVALID_RECORD("Invalid warehouse movement record"),

    // Units of measurement
    UNIT_OF_MEASUREMENT_ALREADY_EXISTS("Unit of measurement already exists"),
    UNIT_OF_MEASUREMENT_NOT_FOUND("Unit of measurement not found"),
    INVALID_UNIT_OF_MEASUREMENT("Invalid unit of measurement format"),
    UNIT_OF_MEASUREMENT_IN_USE("Unit of measurement is already used by products and cannot be deleted."),

    // Files
    UNSUPPORTED_FILE_TYPE("Unsupported file type"),
    INVALID_FILE_NAME("Invalid file name"),
    FILE_NOT_FOUND("File not found"),
    DIRECTORY_CREATION_FAILED("Directory path creation failed"),
    FILE_UPLOAD_FAILED("The file could not be uploaded"),
    FILE_STORAGE_FAILED("Error storing file"),
    UNSUPPORTED_OPERATION("Unsupported operation on the filesystem"),
    FILE_DELETE_FAILED("File could not be deleted"),
    FILE_CONVERTING_FAILED("File could not be converted to Base64"),
    FILE_NAME_IS_EMPTY("File name is empty"),
    FILE_IS_EMPTY("File is empty"),

    // Customers and suppliers
    CUSTOMER_ALREADY_EXISTS("Customer with the same name and address already exists"),
    CUSTOMER_NUMBER_ALREADY_EXISTS("The customer number already exists."),
    CUSTOMER_NOT_FOUND("Customer not found"),

    // Payments
    PAYMENT_NOT_FOUND("Payment not found"),
    PAYMENT_MULTIPLE_REFERENCES("Payment cannot be linked to both a sale and a purchase"),

    // Payment methods
    PAYMENT_METHOD_ALREADY_EXISTS("Payment method already exists"),
    PAYMENT_METHOD_NOT_FOUND("Payment method not found"),
    PAYMENT_METHOD_IN_USE("This payment method cannot be deleted because it is already in use."),

    // Payment processes
    PAYMENT_PROCESS_ALREADY_EXISTS("Payment process already exists"),
    PAYMENT_PROCESS_NOT_FOUND("Payment process not found"),
    PAYMENT_PROCESS_IN_USE("This payment process cannot be deleted because it is already in use."),

    // Purchases
    PURCHASE_NOT_FOUND("Purchase not found"),
    NO_PRODUCT_IN_PURCHASE("No products in purchase"),
    PURCHASE_ALREADY_PAID("Purchase cannot be deleted because it has already been paid"),
    PURCHASE_HAS_PAYMENTS("Purchase cannot be deleted because payments have already been recorded"),
    PURCHASE_VENDOR_CHANGE_HAS_PAYMENTS("Cannot change vendor: purchase has registered payments."),

    // Operations
    INVALID_OPERATION_TYPE("Invalid operation type"),
    INVALID_PAYMENT_STATUS("Invalid payment status"),

    // Document types
    TYPE_OF_DOCUMENT_ALREADY_EXISTS("Type of document already exists"),
    TYPE_OF_DOCUMENT_NOT_FOUND("Type of document not found"),
    TYPE_OF_DOCUMENT_IN_USE("This document type cannot be deleted because it is already in use."),

    // Sales
    SALE_NOT_FOUND("Sale not found"),
    NO_PRODUCT_IN_SALE("No products in sale"),
    SALE_ALREADY_PAID("Sale cannot be deleted because it has already been paid"),
    SALE_HAS_PAYMENTS("Sale cannot be deleted because payments have already been recorded"),
    SALE_UPDATE_ALREADY_PAID("Sale cannot be updated because it has already been paid"),
    SALE_CUSTOMER_CHANGE_HAS_PAYMENTS("Cannot change customer: sale has registered payments."),

    // PDF
    PDF_GENERATION_FAILED("PDF document could not be generated"),
    PDF_REPLACEMENT_FAILED("PDF document could not be replaced"),

    // Tax rates
    TAX_RATE_ALREADY_EXISTS("Tax rate already exists"),
    TAX_RATE_NOT_FOUND("Tax rate not found"),
    DEFAULT_TAX_RATE_NOT_FOUND("Default tax rate not found"),
    DEFAULT_TAX_RATE_CHANGE_FORBIDDEN("The default tax rate cannot be unset without assigning another default rate."),
    DEFAULT_TAX_RATE_DELETE_FORBIDDEN("The default tax rate cannot be deleted."),

    // Discount rates
    DISCOUNT_RATE_ALREADY_EXISTS("Discount rate already exists"),
    DISCOUNT_RATE_NOT_FOUND("Discount rate not found"),
    ZERO_DISCOUNT_RATE_CHANGE_FORBIDDEN("0% discount rate cannot be changed"),
    ZERO_DISCOUNT_RATE_DELETE_FORBIDDEN("0% discount rate cannot be deleted"),

    // Shipping
    SHIPPING_ALREADY_EXISTS("Shipping already exists"),
    SHIPPING_NOT_FOUND("Shipping not found"),
    SHIPPING_IN_USE("This shipping method cannot be deleted because it is already in use."),

    // Terms of payment
    TERM_OF_PAYMENT_ALREADY_EXISTS("Term of payment already exists"),
    TERM_OF_PAYMENT_NOT_FOUND("Term of payment not found"),
    TERM_OF_PAYMENT_IN_USE("This payment term cannot be deleted because it is already in use.");

    private final String description;

    ErrorDescription(String description) {
        this.description = description;
    }
}
