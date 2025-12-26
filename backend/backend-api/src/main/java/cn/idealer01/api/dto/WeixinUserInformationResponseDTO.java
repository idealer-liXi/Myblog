package cn.idealer01.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WeixinMessageResponseDTO {

    private String openId;
    private String weixinName;
    private String weixinImageUrl;

}
