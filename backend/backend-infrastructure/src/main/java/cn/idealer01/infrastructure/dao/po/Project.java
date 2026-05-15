package cn.idealer01.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project {
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
    private Boolean enabled;
    private String accessType;
    private String allowedRoles;
    private Date createTime;
    private Date updateTime;
}
