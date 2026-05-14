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
public class Article {

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
