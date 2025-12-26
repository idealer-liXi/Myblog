package cn.idealer01.domain.auth.adapter.repository;

import cn.idealer01.domain.auth.model.entity.WeixinUserEntity;

public interface ILoginReposity {

    String checkLogin(String ticket);

    void saveLoginState(String ticket, String openId);

    WeixinUserEntity queryWeixinUserByOpenId(String openid);

    void addWeixinUser(String openid);
}
