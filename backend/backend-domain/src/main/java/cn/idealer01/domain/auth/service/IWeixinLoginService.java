package cn.idealer01.domain.auth.service;

import cn.idealer01.api.dto.BindExistingAccountRequestDTO;
import cn.idealer01.api.dto.CurrentUserResponseDTO;
import cn.idealer01.api.dto.LoginResponseDTO;
import cn.idealer01.api.dto.ThirdPartyPendingLoginResponseDTO;
import cn.idealer01.api.dto.CreateThirdPartyAccountRequestDTO;
import cn.idealer01.domain.auth.model.entity.WeixinUserEntity;

import java.io.IOException;

public interface IWeixinLoginService {
    //检查用户是否登录
    Object checkLogin(String ticket);

    //申请创建二维码ticket
    String createQrCodeTicket() throws IOException;

    //保存用户登录状态
    void saveLoginState(String ticket, String openId) throws IOException;

    //持久化用户基本信息：昵称，头像.....
    void saveUserMessgae(String openid);

    WeixinUserEntity getWeixinUser(String openid);

    LoginResponseDTO bindExistingAccount(BindExistingAccountRequestDTO request);

    LoginResponseDTO createAccount(CreateThirdPartyAccountRequestDTO request);

    String createBindQrCodeTicket() throws IOException;

    CurrentUserResponseDTO checkBind(String ticket, String username);

    void unbindWechat(String username);
}

