package cn.idealer01.api.dto;

import lombok.Data;

@Data
public class MusicRequestDTO {
    private String name;
    private String artist;
    private String audioUrl;
    private String coverImage;
    private Integer sortOrder;
    private Boolean enabled;
}
