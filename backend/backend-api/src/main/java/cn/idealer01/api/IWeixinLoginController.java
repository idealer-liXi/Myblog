package cn.idealer01.api;

import cn.idealer01.api.response.Response;

public interface IWeixinLoginController {

    //获取微信登录二维码
    Response<String> weixinQrCodeTicket();
    //检查当前用户是否登录
    Response<Object> checkLogin(String ticket);

}
