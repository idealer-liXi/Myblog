package cn.idealer01.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeixinUser {

    private Long id;

    private String openId;

    private String weixinName;

    private String weixinImageUrl;

    private Date createTime;

}
