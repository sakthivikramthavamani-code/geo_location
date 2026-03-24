package com.georeport.config;

import com.georeport.entity.*;
import com.georeport.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Initializes essential system data (roles and categories) on startup.
 * No demo users are created - users register themselves.
 */
@Configuration
public class DataInitializer {

        @Autowired
        private RoleRepository roleRepository;

        @Autowired
        private IssueCategoryRepository categoryRepository;

        @Bean
        public CommandLineRunner initData() {
                return args -> {
                        // Create roles if not exist (required for authentication)
                        if (roleRepository.findByName(RoleType.ROLE_CITIZEN).isEmpty()) {
                                roleRepository.save(Role.builder()
                                        .name(RoleType.ROLE_CITIZEN)
                                        .description(RoleType.ROLE_CITIZEN.getDescription())
                                        .build());
                        }

                        if (roleRepository.findByName(RoleType.ROLE_ADMIN).isEmpty()) {
                                roleRepository.save(Role.builder()
                                        .name(RoleType.ROLE_ADMIN)
                                        .description(RoleType.ROLE_ADMIN.getDescription())
                                        .build());
                        }

                        if (roleRepository.findByName(RoleType.ROLE_DEPARTMENT).isEmpty()) {
                                roleRepository.save(Role.builder()
                                        .name(RoleType.ROLE_DEPARTMENT)
                                        .description(RoleType.ROLE_DEPARTMENT.getDescription())
                                        .build());
                        }

                        // Create categories if not exist (required for issue reporting)
                        if (categoryRepository.count() == 0) {
                                createCategories();
                        }

                        System.out.println("==============================================");
                        System.out.println("  Geo-Based Issue Reporting System Started!");
                        System.out.println("  Access at: http://localhost:8080");
                        System.out.println("==============================================");
                        System.out.println("  Database: MySQL (georeport_db)");
                        System.out.println("  Mode: Production");
                        System.out.println("==============================================");
                };
        }

        private void createCategories() {
                categoryRepository.save(IssueCategory.builder()
                                .name("Road Damage").description("Potholes, cracks, damaged roads")
                                .icon("road").color("#e74c3c").priority(1).isActive(true).build());

                categoryRepository.save(IssueCategory.builder()
                                .name("Water Leakage").description("Pipeline leaks, water overflow")
                                .icon("water").color("#3498db").priority(2).isActive(true).build());

                categoryRepository.save(IssueCategory.builder()
                                .name("Garbage Overflow").description("Overflowing bins, waste accumulation")
                                .icon("trash").color("#27ae60").priority(3).isActive(true).build());

                categoryRepository.save(IssueCategory.builder()
                                .name("Street Light").description("Non-functional or damaged street lights")
                                .icon("lightbulb").color("#f39c12").priority(4).isActive(true).build());

                categoryRepository.save(IssueCategory.builder()
                                .name("Sanitation").description("Drainage, sewage, public toilets")
                                .icon("warning").color("#9b59b6").priority(5).isActive(true).build());

                categoryRepository.save(IssueCategory.builder()
                                .name("Traffic").description("Signal issues, road signs, markings")
                                .icon("traffic-light").color("#e67e22").priority(6).isActive(true).build());

                categoryRepository.save(IssueCategory.builder()
                                .name("Public Property").description("Parks, benches, public buildings")
                                .icon("building").color("#1abc9c").priority(7).isActive(true).build());

                categoryRepository.save(IssueCategory.builder()
                                .name("Other").description("Other civic issues")
                                .icon("question").color("#95a5a6").priority(8).isActive(true).build());
        }
}
