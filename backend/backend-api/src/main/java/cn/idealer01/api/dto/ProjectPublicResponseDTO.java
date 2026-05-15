package cn.idealer01.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectPublicResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String techStack;
    private String projectUrl;
    private String githubUrl;
    private String previewUrl;
    private String coverImage;
    private String status;
    private Integer sortOrder;
    private String startDate;
    private String endDate;
    private Boolean isPublic;
}
