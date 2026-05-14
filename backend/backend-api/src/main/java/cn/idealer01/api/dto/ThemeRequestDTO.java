package cn.idealer01.api.dto;

import lombok.Data;

@Data
public class ThemeRequestDTO {
    private String name;
    private String icon;
    private String color;
    private Integer sortOrder;
}
