package com.georeport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Geo-Based Public Issue Reporting System.
 * 
 * This application provides a real-time, location-aware platform for citizens
 * to report public infrastructure issues such as road damage, water leakage,
 * garbage overflow, and sanitation failures.
 * 
 * Features:
 * - Geospatial issue reporting with map-based location selection
 * - Real-time status updates via WebSocket
 * - JWT-based authentication
 * - Role-based access control (Citizen, Admin)
 * - PostGIS-powered spatial queries
 * 
 * @author GeoReport Team
 * @version 1.0.0
 */
@SpringBootApplication
public class GeoReportApplication {

    public static void main(String[] args) {
        SpringApplication.run(GeoReportApplication.class, args);
        System.out.println("==============================================");
        System.out.println("  Geo-Based Issue Reporting System Started!");
        System.out.println("  Access at: http://localhost:8081");
        System.out.println("==============================================");
    }
}
