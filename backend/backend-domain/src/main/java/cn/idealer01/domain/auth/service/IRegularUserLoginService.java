package cn.idealer01.domain.auth.service;

public interface IRegularUserLoginService {
    String getToken(String username, String password);
}
