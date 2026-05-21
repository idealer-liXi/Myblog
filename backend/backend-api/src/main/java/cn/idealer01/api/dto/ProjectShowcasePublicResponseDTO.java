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
public class ProjectShowcasePublicResponseDTO {
    private String id;
    private String name;
    private String description;
    private List<String> techStack;
    private String githubUrl;
    private String previewUrl;
    private String coverImage;
    private String status;
    private String startDate;
    private String endDate;
    private List<String> images;
}
