package cn.idealer01.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserAuth {

    private Long id;
    private Long userId;
    private String authType;
    private String authKey;
    private String credential;
    private Integer verified;
    private Date createTime;
    private Date updateTime;
}
