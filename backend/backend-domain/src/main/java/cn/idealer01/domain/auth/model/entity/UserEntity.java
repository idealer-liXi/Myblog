package cn.idealer01.domain.auth.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserEntity {
    private String username;
    private String password;
//    private Long levelId; 根据levelId查询获得levelName用于之后的授权操作
    private String levelName;
    private Integer status;
}
