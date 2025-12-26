package cn.idealer01.api;

import cn.idealer01.api.response.Response;

public interface IWeixinLoginService extends ILoginService {

    //获取微信登录二维码
    Response<String> weixinQrCodeTicket();

}
