package de.ait.smallBusiness_be.constaints;

/**
 * 19.09.2024
 * MashCom_BE
 *
 * @author Kondratyeva (AIT TR)
 */


/**
 * A utility class containing regular expression patterns for validating various fields in entities.
 */
public class EntityValidationConstants {

    /**
     * Regular expression pattern for validating phone numbers.
     * <p>
     * This pattern is designed to validate phone numbers with the following structure:
     * <ul>
     *   <li>Optional '+' sign at the start of the number.</li>
     *   <li>Optional opening parenthesis '(' after the '+' or at the beginning.</li>
     *   <li>Exactly three digits for the area code or first part of the number.</li>
     *   <li>Optional closing parenthesis ')' after the area code.</li>
     *   <li>Optional separator which can be a hyphen '-', a space ' ', or a dot '.' between the area code and the next three digits.</li>
     *   <li>Exactly three digits following the separator.</li>
     *   <li>Optional separator which can be a hyphen '-', a space ' ', or a dot '.' between the second three digits and the final part of the number.</li>
     *   <li>Four to six digits for the final part of the number.</li>
     * </ul>
     * <p>
     * Valid examples:
     * <ul>
     *   <li>+123-456-7890</li>
     *   <li>(123) 456-7890</li>
     *   <li>123.456.7890</li>
     *   <li>1234567890</li>
     *   <li>123-456-789012</li> (supports up to 6 digits in the last part)
     * </ul>
     * <p>
     * Invalid examples:
     * <ul>
     *   <li>12-3456-7890</li> (not enough digits in the area code)</li>
     *   <li>123-45-6789</li> (not enough digits in the middle section)</li>
     *   <li>123-4567-890</li> (not enough digits in the last section, requires a minimum of 4)</li>
     *   <li>abc-def-ghij</li> (contains letters, which are not allowed)</li>
     * </ul>
     */
    public static final String PHONE_REGEX = "^\\+?[(]?[0-9]{3}[)]?[-\\s.]?[0-9]{3}[-\\s.]?[0-9]{4,6}$";

    /**
     * Regular expression pattern for validating restaurant descriptions.
     * <p>
     * This pattern allows descriptions that include:
     * <ul>
     *   <li>Uppercase and lowercase English letters (A-Z, a-z).</li>
     *   <li>Spaces (including tabs and newlines).</li>
     *   <li>German special characters: ü, ö, ä, ß, Ü, Ä, Ö.</li>
     *   <li>Digits (0-9).</li>
     *   <li>Common punctuation and symbols:</li>
     *   <ul>
     *     <li>Hyphens (-), periods (.), commas (,), exclamation marks (!), question marks (?), ampersands (&).</li>
     *     <li>Parentheses (()), square brackets ([]), single quotes ('), double quotes ("), slashes (/).</li>
     *   </ul>
     * </ul>
     * <p>
     * Valid examples:
     * <ul>
     *   <li>"A cozy place with a beautiful view."</li>
     *   <li>"Family-friendly & pet-friendly restaurant!"</li>
     *   <li>"Open (24/7) [No holidays]"</li>
     *   <li>"Try our special: Spätzle!"</li>
     *   <li>"Pasta/Spaghetti & Meatballs"</li>
     *   <li>"Chef's choice: 'Grilled Salmon' served fresh."</li>
     * </ul>
     * <p>
     * Invalid examples:
     * <ul>
     *   <li>"Welcome @ our place"</li> (contains invalid character '@')
     *   <li>"Come #1 for service"</li> (contains invalid character '#')
     *   <li>"Special dish *only today*"</li> (contains invalid character '*')
     * </ul>
     */
    public static final String DESCRIPTION_REGEX = "^[A-Za-züöäßÜÄÖ0-9\\s.,!?&()\\[\\]\\'\"\\/-]+$";

    /**
     * Regular expression pattern for validating restaurant names.
     * <p>
     * This pattern allows restaurant names that include:
     * <ul>
     *   <li>Uppercase and lowercase English letters (A-Z, a-z).</li>
     *   <li>Accented characters from various European languages (e.g., á, é, ü).</li>
     *   <li>Digits (0-9).</li>
     *   <li>Spaces (including tabs and newlines).</li>
     *   <li>Common punctuation and symbols: ampersands (&), hyphens (-), single quotes ('), and apostrophes (’).</li>
     * </ul>
     * <p>
     * Valid examples:
     * <ul>
     *   <li>"Café del Mar"</li>
     *   <li>"O'Leary’s Pub"</li>
     *   <li>"Bäckerei Müller"</li>
     *   <li>"La Petite Boulangerie"</li>
     *   <li>"Mamma Mia's Pizzeria"</li>
     *   <li>"123 Eatery"</li>
     *   <li>"Tacos & Tequila"</li>
     *   <li>"L'Amour Bistro"</li>
     * </ul>
     * <p>
     * Invalid examples:
     * <ul>
     *   <li>"#1 Restaurant"</li> (contains invalid character '#')
     *   <li>"@The Cafe"</li> (contains invalid character '@')
     *   <li>"Best*Ever!"</li> (contains invalid characters '*' and '!')
     *   <li>"New|Old"</li> (contains invalid character '|')
     * </ul>
     */
    public static final String NAME_REGEX = "^[A-Za-zÀ-ÖØ-öø-ÿ0-9\\s&'’‘-]+$";

    /**
     * Regular expression pattern for validating postal codes.
     * <p>
     * This pattern accommodates postal codes for various countries with the following formats:
     * <ul>
     *   <li>United States: 5 digits or 5 digits followed by a hyphen and 4 more digits (e.g., 12345 or 12345-6789).</li>
     *   <li>Canada: A format of letter-digit-letter space digit-letter-digit (e.g., A1A 1A1).</li>
     *   <li>Germany: 5 digits (e.g., 12345).</li>
     *   <li>France, Spain, Italy: 5 digits (e.g., 75008, 28001, 00100).</li>
     *   <li>Netherlands: 4 digits followed by 2 letters (e.g., 1234 AB).</li>
     *   <li>Switzerland: 4 digits (e.g., 8000).</li>
     *   <li>Sweden: 5 digits, optionally separated by a space (e.g., 12345 or 123 45).</li>
     * </ul>
     * <p>
     * Valid examples:
     * <ul>
     *   <li>"12345"</li>
     *   <li>"12345-6789"</li>
     *   <li>"A1A 1A1"</li>
     *   <li>"75008"</li>
     *   <li>"1234 AB"</li>
     *   <li>"8000"</li>
     *   <li>"12345"</li>
     *   <li>"123 45"</li>
     * </ul>
     * <p>
     * Invalid examples:
     * <ul>
     *   <li>"1234"</li> (too short for most formats that require 5 digits or more)
     *   <li>"123456"</li> (too long for most formats that require exactly 5 digits or specific format lengths)
     *   <li>"ABCDE"</li> (contains non-numeric and non-valid letters for numeric formats)
     *   <li>"12 34"</li> (incorrect spacing format)
     * </ul>
     */

    public static final String RESTAURANT_POSTAL_CODE_REGEX = "^([A-Z]{1,2}\\d[A-Z\\d]? \\d[A-Z]{2}|\\d{5}(-\\d{4})?|[A-Z]\\d[A-Z] \\d[A-Z]\\d|\\d{4}|\\d{4} [A-Z]{2}|\\d{3} \\d{2}|\\d{5})$";

    /**
     * Regular expression pattern for Country and City names validation.
     * <p>
     * This pattern allows city names that include:
     * <ul>
     *   <li>Uppercase and lowercase English letters (A-Z, a-z).</li>
     *   <li>Spaces.</li>
     *   <li>Hyphens (-).</li>
     *   <li>Apotrophes (').</li>
     *   <li>Dots ('.').</li>
     *   <li>German special characters: ü, ö, ä, ß, Ü, Ä, Ö.</li>
     * </ul>
     * <p>
     * The pattern also ensures that:
     * <ul>
     *   <li>Leading and trailing spaces are not allowed.</li>
     *   <li>Two or more consecutive spaces are not allowed.</li>
     * </ul>
     * <p>
     * Valid examples:
     * <ul>
     *   <li>"New York"</li>
     *   <li>"San Francisco"</li>
     *   <li>"München"</li>
     *   <li>"Los-Angeles"</li>
     *   <li>"St. Petersburg"</li>
     * </ul>
     * <p>
     * Invalid examples:
     * <ul>
     *   <li>"  New York"</li> (leading space)</li>
     *   <li>"New   York"</li> (contains two consecutive spaces)</li>
     *   <li>"San Francisco "</li> (trailing space)</li>
     *   <li>"St. Petersburg!"</li> (contains invalid character '!')</li>
     *   <li>"Berlin@"</li> (contains invalid character '@')</li>
     *   <li>"München,"</li> (contains invalid character ',')</li>
     * </ul>
     */

    public static final String RESTAURANT_COUNTRY_CITY_REGEX = "^[A-Za-zÄÖÜäöüß\\-\\s'\\.]+$";


    /**
     * Regular expression pattern for validating street names.
     * <p>
     * This pattern allows street names that include:
     * <ul>
     *   <li>Uppercase and lowercase English letters (A-Z, a-z).</li>
     *   <li>Spaces.</li>
     *   <li>German special characters: ü, ö, ä, ß, Ü, Ä, Ö.</li>
     *   <li>Digits (0-9).</li>
     *   <li>Hyphens (-).</li>
     *   <li>Apotrophes (').</li>
     *   <li>Dots ('.').</li>
     * </ul>
     * <p>
     * The pattern also ensures that:
     * <ul>
     *   <li>Leading and trailing spaces are not allowed.</li>
     *   <li>Two or more consecutive spaces are not allowed.</li>
     * </ul>
     * <p>
     * Valid examples:
     * <ul>
     *   <li>"Baker's Street"</li>
     *   <li>"5th Ave"</li>
     *   <li>"Main Street"</li>
     *   <li>"Müllerstraße"</li>
     *   <li>"12-Street"</li>
     * </ul>
     * <p>
     * Invalid examples:
     * <ul>
     *   <li>"Main  Street"</li> (contains two consecutive spaces)</li>
     *   <li>"  Baker's Street"</li> (leading space)</li>
     *   <li>"Baker's Street "</li> (trailing space)</li>
     *   <li>"Baker's@Street"</li> (contains invalid character '@')</li>
     *   <li>"Main Street!"</li> (contains invalid character '!')</li>
     *   <li>"5th Avenue,"</li> (contains invalid character ',')</li>
     * </ul>
     */

    public static final String RESTAURANT_STREET_REGEX = "^[A-Za-z0-9ÄÖÜäöüß\\-\\s'\\.]+$";

    /**
     * Regular expression pattern for validating house numbers.
     * <p>
     * This pattern allows house numbers that include:
     * <ul>
     *   <li>Uppercase and lowercase English letters (A-Z, a-z).</li>
     *   <li>Digits (0-9).</li>
     *   <li>Hyphens (-).</li>
     * </ul>
     * <p>
     * The pattern also ensures that:
     * <ul>
     *   <li>Leading and trailing spaces are not allowed.</li>
     *   <li>Two or more consecutive spaces are not allowed.</li>
     * </ul>
     * <p>
     * Valid examples:
     * <ul>
     *   <li>"123"</li>
     *   <li>"A12"</li>
     *   <li>"12-B"</li>
     *   <li>"10A-3"</li>
     * </ul>
     * <p>
     * Invalid examples:
     * <ul>
     *   <li>" 123"</li> (leading space)</li>
     *   <li>"123 "</li> (trailing space)</li>
     *   <li>"12 34"</li> (contains two consecutive spaces)</li>
     *   <li>"12@34"</li> (contains invalid character '@')</li>
     *   <li>"123--456"</li> (contains multiple consecutive hyphens)</li>
     * </ul>
     */

    public static final String RESTAURANT_BUILDING_REGEX = "^(?!\\s)(?!.*\\s{2,})[A-Za-z0-9-]+(?<!\\s)$";

    /**
     * Regular expression pattern for validating restaurant category names.
     * <p>
     * This pattern allows category names that include:
     * <ul>
     *   <li>Uppercase and lowercase English letters (A-Z, a-z).</li>
     *   <li>German special characters: Ä, Ö, Ü, ä, ö, ü, ß.</li>
     *   <li>Spaces.</li>
     * </ul>
     * <p>
     * The following restrictions apply:
     * <ul>
     *   <li>No leading or trailing spaces.</li>
     *   <li>No consecutive spaces (i.e., no two or more spaces in a row).</li>
     * </ul>
     * <p>
     * Valid examples:
     * <ul>
     *   <li>"Italian"</li>
     *   <li>"Mexican"</li>
     *   <li>"Café Ältere"</li>
     *   <li>"Asian Fusion"</li>
     * </ul>
     * <p>
     * Invalid examples:
     * <ul>
     *   <li>"  Italian"</li> (contains leading spaces)
     *   <li>"Italian  "</li> (contains trailing spaces)
     *   <li>"Mexican  Cuisine"</li> (contains consecutive spaces)
     * </ul>
     */

    public static final String RESTAURANT_CATEGORY_REGEX ="^(?!\\s)(?!.*\\s{2,})[A-Za-z\\sÄÖÜäöüß]+(?<!\\s)$";


    /**
     * Regular expression pattern for validating email addresses.
     * <p>
     * This pattern enforces a basic structure for email addresses:
     * <ul>
     *   <li>Local part before the '@' symbol: consists of any character except whitespace, '@', or control characters.</li>
     *   <li>Domain part after the '@' symbol: requires at least one period (.) separating the domain name and the top-level domain (e.g., .com, .org).</li>
     *   <li>Ensures there are no whitespace characters or control characters throughout the email.</li>
     * </ul>
     * <p>
     * Valid examples:
     * <ul>
     *   <li>"user@example.com"</li>
     *   <li>"john.doe@company.org"</li>
     *   <li>"name@sub.domain.com"</li>
     *   <li>"email+alias@domain.co.uk"</li>
     * </ul>
     * <p>
     * Invalid examples:
     * <ul>
     *   <li>"user@.com"</li> (missing domain name)
     *   <li>"@example.com"</li> (missing local part)
     *   <li>"user@com"</li> (missing period in the domain)
     *   <li>"user@domain,com"</li> (invalid character ',')
     * </ul>
     */
    public static final String EMAIL_REGEX = "^[^@ \\t\\r\\n]+@[^@ \\t\\r\\n]+\\.[^@ \\t\\r\\n]+$";

    /**
     * Regular expression pattern for validating user first/last names.
     * <p>
     * This pattern allows first and last names that include:
     * <ul>
     *   <li>Uppercase and lowercase English letters (A-Z, a-z).</li>
     *   <li>Spaces.</li>
     *   <li>German special characters: ü, ö, ä, ß, Ü, Ä, Ö.</li>
     *   <li>Digits (0-9).</li>
     *   <li>Hyphens (-).</li>
     * </ul>
     * <p>
     * Valid examples:
     * <ul>
     *   <li>"John"</li>
     *   <li>"Anne-Marie"</li>
     *   <li>"Jörg-Übel"</li>
     *   <li>"Müller 123"</li>
     *   <li>"Doe"</li>
     *   <li>"Van der Linden"</li>
     *   <li>"Schwarz-Öhring"</li>
     *   <li>"123 Schmidt"</li>
     * </ul>
     * <p>
     * Invalid examples:
     * <ul>
     *   <li>"John!"</li> (contains invalid character '!')
     *   <li>"Peter@"</li> (contains invalid character '@')
     * </ul>
     */
    public static final String FIRST_LAST_NAME_REGEX = "^[A-Za-züöäßÜÄÖ0-9\\-\\s]+$";

    /**
     * Regular expression pattern for validating passwords.
     * <p>
     * This pattern enforces passwords that must include:
     * <ul>
     *   <li>At least one uppercase English letter (A-Z).</li>
     *   <li>At least one digit (0-9).</li>
     *   <li>At least one special character (@, $, !, %, *, ?, &).</li>
     *   <li>Can also include lowercase English letters (a-z).</li>
     * </ul>
     * Note: Passwords should be between 8 to 50 characters, as enforced by the @Size annotation!.
     * <p>
     * Valid examples:
     * <ul>
     *   <li>"Password1!"</li>
     *   <li>"Secure@123"</li>
     *   <li>"Admin#2022"</li>
     * </ul>
     * <p>
     * Invalid examples:
     * <ul>
     *   <li>"password"</li> (missing uppercase, digit, and special character)
     *   <li>"12345678"</li> (missing uppercase and special character)
     *   <li>"PASSWORD!"</li> (missing digit)
     * </ul>
     */
    public static final String PASSWORD_REGEX = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$";

}

