package com.georeport.dto;

import lombok.*;

/**
 * DTO for issue category response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {

    private Long id;
    private String name;
    private String description;
    private String icon;
    private String color;
    private Integer priority;
    private Boolean isActive;
}
