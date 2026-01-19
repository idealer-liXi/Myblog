package cn.idealer01.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserLevel {

    private Long id;
    private Integer levelId;
    private String name;
    private String description;
    private Integer priority;
    private Date createTime;
    private Date updateTime;

}
