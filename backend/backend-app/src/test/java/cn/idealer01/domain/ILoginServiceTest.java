package cn.idealer01.domain;

import cn.idealer01.domain.auth.model.entity.WeixinUserEntity;
import cn.idealer01.domain.auth.service.ILoginService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ILoginServiceTest {

    @Resource
    private ILoginService loginService;

    @Test
    public void test_saveUserMessgae(){
        String openid = "0000000";
        loginService.saveUserMessgae(openid);
    }

    @Test
    public void test_getWeixinUser(){
        String openid = "0000000";
        WeixinUserEntity weixinUser = loginService.getWeixinUser(openid);
        System.out.println(JSONObject.toJSONString(weixinUser));
    }

}
