package cn.idealer01.domain.auth.adapter.repository;

public interface ILoginReposity {

    String checkLogin(String ticket);

    void saveLoginState(String ticket, String openId);
}
