package com.thinklab.domain.valueobject;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import java.util.Objects;

/**
 * Domain Value Object: Encapsulates the global identity, localization, and
 * legal metadata for an Organization (Tenant).
 *
 * <p>This record acts as a high-fidelity data structure designed to support
 * multi-national operations, ensuring that tax identification and regional
 * settings are sanitized and immutable throughout the business lifecycle.</p>
 *
 * <p><b>Architectural Principles:</b></p>
 * <ul>
 * <li><b>Immutability:</b> Java Record ensures thread-safety in reactive pipelines.</li>
 * <li><b>Self-Validation:</b> Defensive compact constructor prevents corrupted data entry.</li>
 * <li><b>Localization-Aware:</b> Groups ISO codes, timezones, and languages for global scale.</li>
 * </ul>
 *
 * @param taxId          Global Tax Identification Number (e.g., CNPJ, VAT, EIN).
 * @param corporateEmail Official communication channel for administrative alerts.
 * @param countryCode    ISO 3166-1 alpha-2 country code (e.g., "BR", "US").
 * @param city           Name of the legal headquarters city.
 * @param timezone       IANA timezone identifier (e.g., "America/Sao_Paulo").
 * @param language       Primary operational language (e.g., "pt", "en").
 * @param industry       Industrial classification or business sector.
 */
@Introspected // Gera o código de introspecção em tempo de compilação
@Serdeable    // Define que este objeto é serializável pelo Micronaut Serde
public record TenantProfile(
        String taxId,
        String corporateEmail,
        String countryCode,
        String city,
        String timezone,
        String language,
        String industry
) {

    /**
     * Compact constructor for defensive programming and input sanitization.
     * Enforces that mandatory identity fields are never null or blank.
     */
    public TenantProfile {
        Objects.requireNonNull(taxId, "taxId cannot be null");
        Objects.requireNonNull(corporateEmail, "corporateEmail cannot be null");
        Objects.requireNonNull(countryCode, "countryCode cannot be null");

        // Sanitization and blank check
        taxId = taxId.trim();
        corporateEmail = corporateEmail.trim().toLowerCase();
        countryCode = countryCode.trim().toUpperCase();
        city = city != null ? city.trim() : "N/A";
        timezone = timezone != null ? timezone.trim() : "UTC";
        language = language != null ? language.trim().toLowerCase() : "en";
        industry = industry != null ? industry.trim().toUpperCase() : "GENERAL";

        if (taxId.isBlank()) throw new IllegalArgumentException("Tax ID must be provided for organization identity.");
        if (corporateEmail.isBlank()) throw new IllegalArgumentException("Corporate email is mandatory for tenant registration.");
        if (countryCode.length() != 2) throw new IllegalArgumentException("Country code must follow ISO 3166-1 alpha-2 standard.");
    }

    /**
     * Utility to retrieve the industry classification with a safe fallback.
     */
    public String getIndustrySafely() {
        return (industry == null || industry.isBlank()) ? "GENERAL" : industry;
    }
}