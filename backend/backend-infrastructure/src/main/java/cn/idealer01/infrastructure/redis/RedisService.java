package cn.idealer01.infrastructure.redis;

import cn.idealer01.types.common.Constants;
import lombok.Data;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Repository
public class RedisService implements IRedisService{
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public String checkLogin(String ticket) {
        return stringRedisTemplate.opsForValue().get(Constants.TICKET_OPENID_PREFIX + ticket);
    }

    @Override
    public String queryAccessTokenByAppId(String appId) {
        return stringRedisTemplate.opsForValue().get(Constants.APPID_ACCESSTOKEN_PREFIX + appId);
    }

    @Override
    public void saveAccessToken(String appId, String accessToken) {
        stringRedisTemplate.opsForValue().set(Constants.APPID_ACCESSTOKEN_PREFIX + appId, accessToken, 7000, TimeUnit.SECONDS);
    }

    @Override
    public void saveLoginState(String ticket, String openId) {
        stringRedisTemplate.opsForValue().set(Constants.TICKET_OPENID_PREFIX + ticket, openId, 7000, TimeUnit.SECONDS);
    }


}
