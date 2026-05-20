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
public class ProfileSchoolHonorsRecord {
    private Long id;
    private String schoolKey;
    private String honors;
    private Date createTime;
    private Date updateTime;
}
