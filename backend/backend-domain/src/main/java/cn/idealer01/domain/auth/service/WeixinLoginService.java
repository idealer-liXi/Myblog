package cn.idealer01.domain.auth.service;

import cn.idealer01.domain.auth.adapter.port.ILoginPort;
import cn.idealer01.domain.auth.adapter.repository.ILoginRepository;
import cn.idealer01.domain.auth.model.entity.WeixinUserEntity;
import org.springframework.stereotype.Service;


import javax.annotation.Resource;
import java.io.IOException;

@Service("WeixinLoginService")
public class WeixinLoginService implements IWeixinLoginService {
    @Resource
    private ILoginPort loginPort;
    @Resource
    private ILoginRepository loginReposity;

    /**
     * 创建二维码，要调用外部api，故交给基础设施层实现
     * @return 二维码
     */
    @Override
    public String createQrCodeTicket() throws IOException {
        return loginPort.createQrCodeTicket();
    }

    /**
     * 引入，查询redis是否缓存openId
     * @param ticket
     * @return redis查询结果
     */
    @Override
    public String checkLogin(String ticket) {
        return loginReposity.checkLogin(ticket);
    }

    /**
     * 将ticket，openId作为k-v保存到数据库
     * @param ticket
     * @param openId
     */
    @Override
    public void saveLoginState(String ticket, String openId) throws IOException {
        //1.将k-v保存
        loginReposity.saveLoginState(ticket, openId);
        //2.在公众号给用户发送提示消息
        loginPort.sendLoginTemplate(openId);
    }

    @Override
    public void saveUserMessgae(String openid) {
        //1.查询数据库中用户信息是否存在
        WeixinUserEntity user = loginReposity.queryWeixinUserByOpenId(openid);
        //2.不存在，则新添加用户信息
        if(null == user){
            loginReposity.addWeixinUser(openid);
        }
    }

    @Override
    public WeixinUserEntity getWeixinUser(String openid) {
        return loginReposity.queryWeixinUserByOpenId(openid);
    }
}
