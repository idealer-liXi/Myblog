package cn.idealer01.infrastructure.redis;

import org.springframework.stereotype.Service;

@Service
public interface IRedisService {

    String checkLogin(String ticket);

    void saveLoginState(String ticket, String openId);

    String queryAccessTokenByAppId(String appId);

    void saveAccessToken(String appId, String accessToken);
}
