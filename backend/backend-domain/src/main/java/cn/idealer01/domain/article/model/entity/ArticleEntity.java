package cn.idealer01.domain.article.model.entity;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleEntity {

    private Long id;
    private String title;
    private String content;
    private String summary;
    private String theme;
    private String coverImage;
    private String status;
    private Long authorId;
    private Integer viewCount;
    private Date createTime;
    private Date updateTime;
}
