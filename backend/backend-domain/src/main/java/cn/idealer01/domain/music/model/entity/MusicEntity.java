package cn.idealer01.domain.music.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MusicEntity {
    private Long id;
    private String name;
    private String artist;
    private String audioUrl;
    private String coverImage;
    private Integer sortOrder;
    private Boolean enabled;
    private Date createTime;
    private Date updateTime;
}
