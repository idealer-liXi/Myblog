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
public class Theme {

    private Long id;
    private String name;
    private String icon;
    private String color;
    private Integer sortOrder;
    private Integer articleCount;
    private Date createTime;
    private Date updateTime;
}
