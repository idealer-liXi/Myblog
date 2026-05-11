package cn.idealer01.types.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class Constants {

    public final static String SPLIT = ",";

    public final static String TICKET_OPENID_PREFIX = "tickct:openid:";

    public final static String APPID_ACCESSTOKEN_PREFIX = "appid:access_token:";


    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public enum ResponseCode{
        SUCCESS("0000", "调用成功"),
        UN_ERROR("0001", "调用失败"),
        ILLEGAL_PARAMETER("0002", "非法参数"),
        NO_LOGIN("1001", "未登录"),
        ;


        private String code;
        private String info;
    }


}
