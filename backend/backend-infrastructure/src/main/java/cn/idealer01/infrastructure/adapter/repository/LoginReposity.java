package cn.idealer01.infrastructure.adapter.repository;

import cn.idealer01.domain.auth.adapter.repository.ILoginReposity;
import cn.idealer01.infrastructure.redis.IRedisService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class LoginReposity implements ILoginReposity {
    @Resource
    private IRedisService redisService;

    @Override
    public String checkLogin(String ticket) {
        return redisService.checkLogin(ticket);
    }

    @Override
    public void saveLoginState(String ticket, String openId) {
        redisService.saveLoginState(ticket, openId);
    }
}
