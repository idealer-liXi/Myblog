package cn.idealer01.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum ResponseCode {

    SUCCESS("0000", "成功"),
    UN_ERROR("0001", "未知失败"),
    ILLEGAL_PARAMETER("0002", "非法参数"),
    LOGIN_ERROR("0003", "账号或密码错误"),
    JWT_INVALID("0004", "JWT失效"),
    USER_NOT_EXIST("0005", "用户不存在"),
    USER_EXIST("0006", "用户已存在"),
    LEVEL_NOT_EXIST("0006", "用户级别不存在"),
    REGISTER_FAIL("0007", "注册失败"),

    ;

    private String code;
    private String info;

}
