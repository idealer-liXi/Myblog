package cn.idealer01.api.dto;

import lombok.Data;

@Data
public class ArticleRequestDTO {
    private String title;
    private String content;
    private String summary;
    private String theme;
    private String coverImage;
    private String status;
}
