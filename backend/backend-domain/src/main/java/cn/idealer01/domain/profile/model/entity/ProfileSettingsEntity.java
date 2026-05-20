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
public class ProfileSettingsEntity {
    private Long id;
    private String avatar;
    private String name;
    private String bio;
    private String email;
    private String githubName;
    private String githubUrl;
    private String location;
    private String introduction;
    private String hobbies;
    private Date createTime;
    private Date updateTime;
}
