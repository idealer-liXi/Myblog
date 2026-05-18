package cn.idealer01.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUserResponseDTO {
    private Long id;
    private String username;
    private String displayName;
    private String avatar;
    private String avatarSource;
    private String effectiveAvatar;
    private String defaultAvatar;
    private String loginType;
    private List<String> roles;
    private String status;
    private Boolean weixinBound;
    private String weixinName;
    private String weixinImageUrl;
    private Date createdAt;
    private Date updatedAt;
}
