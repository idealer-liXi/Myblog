package cn.idealer01.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MusicAdminResponseDTO {
    private Long id;
    private String name;
    private String artist;
    private String audioUrl;
    private String coverImage;
    private Integer sortOrder;
    private Boolean enabled;
    private String createdAt;
    private String updatedAt;
}
