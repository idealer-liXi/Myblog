package cn.idealer01.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThemeResponseDTO {
    private Long id;
    private String name;
    private String icon;
    private String color;
    private Integer sortOrder;
    private Integer articleCount;
    private String createdAt;
    private String updatedAt;
}
