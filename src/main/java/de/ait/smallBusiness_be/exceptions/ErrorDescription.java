package de.ait.smallBusiness_be.exceptions;


import lombok.Getter;

/**
 * SmallBusiness_BE
 * 24.10.2024
 *
 * @author Kondratyeva
 */

@Getter
public enum ErrorDescription {

    EMAIL_ALREADY_EXISTS("Email already exists"),
    USERNAME_ALREADY_EXISTS("Username already exists"),
    EMAIL_NOT_EXISTS("Email does not exist"),
    INVALID_EMAIL("Invalid email"),
    INVALID_USERNAME("Invalid username"),
    INVALID_PASSWORD("Invalid password"),
    USER_NOT_FOUND("User not found"),
    USER_ALREADY_EXISTS("User already exists"),
    FORBIDDEN("Forbidden"),
    UNAUTHORIZED("Unauthorized"),
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

    CATEGORY_NOT_FOUND("Category not found"),
    CATEGORY_ALREADY_EXISTS("Category already exists"),

    PRODUCT_NOT_FOUND("Product not found"),
    PRODUCT_ALREADY_EXISTS("Product with the same name and article already exists."),
    LIST_PRODUCTS_IS_EMPTY("List of products is empty"),

    INVALID_UNIT_OF_MEASUREMENT("Invalid unit of measurement format"),
    UNSUPPORTED_FILE_TYPE("Unsupported file type"),
    INVALID_FILE_NAME("Invalid file name"),
    FILE_NOT_FOUND("File not found"),
    DIRECTORY_CREATION_FAILED("Directory path creation failed"),
    FILE_UPLOAD_FAILED("The file could not be uploaded"),
    FILE_STORAGE_FAILED("Error storing file"),
    UNSUPPORTED_OPERATION("Unsupported operation on the filesystem"),
    FILE_DELETE_FAILED("File could not be deleted"),
    FILE_CONVERTING_FAILED("File could not be converted to Base64"),
    CUSTOMER_ALREADY_EXISTS("Customer already exists");

    private final String description;

    ErrorDescription(String description) {
        this.description = description;
    }
}
