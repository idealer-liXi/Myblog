package cn.idealer01.domain.profile.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileSchoolHonorsEntity {
    private Long id;
    private String schoolKey;
    private String honors;
    private Date createTime;
    private Date updateTime;
}
