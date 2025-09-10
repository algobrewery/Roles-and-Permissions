package com.algobrewery.auth.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Utility class for validating required headers in requests.
 */
public class HeaderValidationUtil {
    
    public static final String APP_USER_UUID_HEADER = "x-app-user-uuid";
    public static final String APP_ORG_UUID_HEADER = "x-app-org-uuid";
    
    /**
     * Validates that required headers are present and not empty.
     * 
     * @param request the HTTP request
     * @throws ResponseStatusException if required headers are missing or empty
     */
    public static void validateRequiredHeaders(HttpServletRequest request) {
        String organizationUuid = request.getHeader(APP_ORG_UUID_HEADER);
        String userUuid = request.getHeader(APP_USER_UUID_HEADER);
        
        if (organizationUuid == null || organizationUuid.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Missing required header: " + APP_ORG_UUID_HEADER);
        }
        
        if (userUuid == null || userUuid.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Missing required header: " + APP_USER_UUID_HEADER);
        }
    }
    
    /**
     * Validates that organization UUID header is present and not empty.
     * 
     * @param request the HTTP request
     * @throws ResponseStatusException if organization UUID header is missing or empty
     */
    public static void validateOrganizationHeader(HttpServletRequest request) {
        String organizationUuid = request.getHeader(APP_ORG_UUID_HEADER);
        
        if (organizationUuid == null || organizationUuid.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Missing required header: " + APP_ORG_UUID_HEADER);
        }
    }
    
    /**
     * Validates that user UUID header is present and not empty.
     * 
     * @param request the HTTP request
     * @throws ResponseStatusException if user UUID header is missing or empty
     */
    public static void validateUserHeader(HttpServletRequest request) {
        String userUuid = request.getHeader(APP_USER_UUID_HEADER);
        
        if (userUuid == null || userUuid.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Missing required header: " + APP_USER_UUID_HEADER);
        }
    }
    
    /**
     * Gets the organization UUID from the request header.
     * 
     * @param request the HTTP request
     * @return the organization UUID
     * @throws ResponseStatusException if organization UUID header is missing or empty
     */
    public static String getOrganizationUuid(HttpServletRequest request) {
        validateOrganizationHeader(request);
        return request.getHeader(APP_ORG_UUID_HEADER);
    }
    
    /**
     * Gets the user UUID from the request header.
     * 
     * @param request the HTTP request
     * @return the user UUID
     * @throws ResponseStatusException if user UUID header is missing or empty
     */
    public static String getUserUuid(HttpServletRequest request) {
        validateUserHeader(request);
        return request.getHeader(APP_USER_UUID_HEADER);
    }
}
