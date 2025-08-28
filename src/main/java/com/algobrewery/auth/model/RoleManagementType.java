package com.algobrewery.auth.model;

/**
 * Enumeration for role management types.
 * Defines how roles are managed in the system.
 */
public enum RoleManagementType {
    /**
     * Role created and managed within customer organization
     */
    CUSTOMER_MANAGED("customer_managed"),
    
    /**
     * Role created and centrally managed by AlgoBrewery
     */
    SYSTEM_MANAGED("system_managed");

    private final String value;

    RoleManagementType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static RoleManagementType fromValue(String value) {
        for (RoleManagementType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown role management type: " + value);
    }
}
