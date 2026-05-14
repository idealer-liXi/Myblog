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
public class ArticleListResponseDTO {
    private long total;
    private int page;
    private int pageSize;
    private List<ArticleItemDTO> articles;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArticleItemDTO {
        private Long id;
        private String title;
        private String summary;
        private String theme;
        private String coverImage;
        private String status;
        private int viewCount;
        private String createdAt;
    }
}
