package cn.idealer01.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDetailResponseDTO {
    private Long id;
    private String title;
    private String content;
    private String summary;
    private String theme;
    private String coverImage;
    private String status;
    private Long authorId;
    private int viewCount;
    private String createdAt;
    private String updatedAt;
}
