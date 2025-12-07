package cn.idealer01.domain.auth.service;

import java.io.IOException;

public interface ILoginService {

    //申请创建二维码ticket
    String createQrCodeTicket() throws IOException;

    //检查用户是否登录
    String checkLogin(String ticket);

    //保存用户登录状态
    void saveLoginState(String ticket, String openId) throws IOException;

}
