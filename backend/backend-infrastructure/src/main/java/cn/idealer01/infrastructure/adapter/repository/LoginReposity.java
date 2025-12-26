package cn.idealer01.infrastructure.adapter.repository;

import cn.idealer01.domain.auth.adapter.repository.ILoginReposity;
import cn.idealer01.domain.auth.model.entity.WeixinUserEntity;
import cn.idealer01.infrastructure.dao.IWeixinUserDao;
import cn.idealer01.infrastructure.dao.po.WeixinUser;
import cn.idealer01.infrastructure.gateway.IWeixinApiService;
import cn.idealer01.infrastructure.redis.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
@Slf4j
public class LoginReposity implements ILoginReposity {
    @Resource
    private IRedisService redisService;
    @Resource
    private IWeixinUserDao weixinUserDao;
    @Resource
    private IWeixinApiService weixinApiService;

    @Override
    public String checkLogin(String ticket) {
        return redisService.checkLogin(ticket);
    }

    @Override
    public void saveLoginState(String ticket, String openId) {
        redisService.saveLoginState(ticket, openId);
    }

    @Override
    public WeixinUserEntity queryWeixinUserByOpenId(String openid) {
        log.info("查询用户{}是否存在", openid);
        WeixinUser weixinUser = weixinUserDao.queryWeixinUserByOpenId(openid);
        if(null == weixinUser) return null;
        return WeixinUserEntity.builder()
                .weixinName(weixinUser.getWeixinName())
                .weixinImageUrl(weixinUser.getWeixinImageUrl())
                .build();
    }

    @Override
    public void addWeixinUser(String openid) {
        log.info("将用户{}信息保存至数据库",openid);
        //1.查询微信信息，微信名称，微信头像
        //WeixinUser weixinUser = weixinApiService.getUserInfo();
        WeixinUser weixinUser = new WeixinUser();
        weixinUser.setWeixinName("weixin-" + openid.substring(0, 4));
        weixinUser.setWeixinImageUrl("https://i1.hdslb.com/bfs/face/d3a9fa7b57d61470b32c29b9a113958dd078b3c7.jpg@150w_150h.jpg");
        weixinUser.setOpenId(openid);
        //2.保存微信用户信息
        weixinUserDao.addWeixinUser(weixinUser);
    }
}
