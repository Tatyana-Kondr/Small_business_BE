package de.ait.smallBusiness_be.customers.services;

import java.text.Normalizer;
import java.util.Locale;

public final class CustomerDuplicateNormalizer {

    private CustomerDuplicateNormalizer() {
    }

    public static String normalizeName(String value) {
        return normalizeText(value);
    }

    public static String normalizeCity(String value) {
        return normalizeText(value);
    }

    public static String normalizePostalCode(String value) {
        if (value == null) return "";

        return value.trim()
                .replaceAll("\\s+", "")
                .toUpperCase(Locale.ROOT);
    }

    public static String normalizeStreet(String value) {
        String street = normalizeText(value);

        // Нормализация немецких сокращений
        street = street.replaceAll(
                "(?U)\\bstr\\.?$",
                "straße"
        );

        street = street.replaceAll(
                "(?U)\\bstr\\.\\s+",
                "straße "
        );

        return street;
    }

    public static String normalizeBuilding(String value) {
        if (value == null) return "";

        return value.trim()
                .replaceAll("\\s+", "")
                .toLowerCase(Locale.ROOT);
    }

    public static String normalizeCountry(String value) {
        if (value == null) return "";

        return value.trim().toUpperCase(Locale.ROOT);
    }

    private static String normalizeText(String value) {
        if (value == null) return "";

        return Normalizer.normalize(value, Normalizer.Form.NFKC)
                .trim()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }
}

