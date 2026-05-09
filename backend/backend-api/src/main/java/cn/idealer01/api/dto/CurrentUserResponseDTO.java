package cn.idealer01.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String loginType;
    private List<String> roles;
    private Boolean weixinBound;
    private String weixinName;
}
