package cn.idealer01.api.dto;

import lombok.Data;

@Data
public class ProjectRequestDTO {
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
    private Boolean enabled;
    private String accessType;
    private String allowedRoles;
}
