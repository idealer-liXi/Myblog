package cn.idealer01.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectAdminResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String techStack;
    private String projectUrl;
    private String githubUrl;
    private String previewUrl;
    private String coverImage;
    private List<String> showcaseImages;
    private String status;
    private Integer sortOrder;
    private String startDate;
    private String endDate;
    private Boolean isPublic;
    private Boolean enabled;
    private String accessType;
    private String allowedRoles;
    private String createdAt;
    private String updatedAt;
}
