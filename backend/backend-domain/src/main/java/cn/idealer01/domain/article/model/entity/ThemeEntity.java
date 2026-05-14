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
public class ThemeEntity {

    private Long id;
    private String name;
    private String icon;
    private String color;
    private Integer sortOrder;
    private Integer articleCount;
    private Date createTime;
    private Date updateTime;
}
