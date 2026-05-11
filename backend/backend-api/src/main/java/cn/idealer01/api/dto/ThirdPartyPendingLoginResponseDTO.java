package cn.idealer01.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThirdPartyPendingLoginResponseDTO {
    private String status;
    private String pendingToken;
    private String authType;
    private String authKey;
    private String displayName;
    private String avatar;
    private String weixinName;
}
