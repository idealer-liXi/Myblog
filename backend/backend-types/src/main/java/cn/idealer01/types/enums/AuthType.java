package cn.idealer01.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthType {
    PASSWORD("password"),
    WECHAT("wechat"),
    EMAIL("email");

    private final String code;
}
