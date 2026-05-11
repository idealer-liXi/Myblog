package cn.idealer01.domain.auth.service.login;

import cn.idealer01.api.dto.BindExistingAccountRequestDTO;
import cn.idealer01.api.dto.CurrentUserResponseDTO;
import cn.idealer01.api.dto.CreateThirdPartyAccountRequestDTO;
import cn.idealer01.api.dto.LoginResponseDTO;
import cn.idealer01.api.dto.ThirdPartyPendingLoginResponseDTO;
import cn.idealer01.domain.auth.adapter.port.ILoginPort;
import cn.idealer01.domain.auth.adapter.repository.ILoginRepository;
import cn.idealer01.domain.auth.model.entity.WeixinUserEntity;
import cn.idealer01.domain.auth.service.IWeixinLoginService;
import cn.idealer01.types.enums.AuthType;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import cn.idealer01.types.utils.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;


import javax.annotation.Resource;
import java.io.IOException;

@Service("WeixinLoginService")
public class WeixinLoginService implements IWeixinLoginService {
    @Resource
    private ILoginPort loginPort;
    @Resource
    private ILoginRepository loginReposity;
    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private AuthenticationManager authenticationManager;

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
    public Object checkLogin(String ticket) {
        String openId = loginReposity.checkLogin(ticket);
        if (StringUtils.isBlank(openId)) {
            return null;
        }

        Long userId = loginReposity.queryUserIdByAuthTypeAndAuthKey(AuthType.WECHAT.getCode(), openId);
        WeixinUserEntity weixinUser = loginReposity.queryWeixinUserByOpenId(openId);

        if (null != userId) {
            CurrentUserResponseDTO profile = loginReposity.queryCurrentUserByUserId(userId);
            profile.setLoginType(AuthType.WECHAT.getCode());
            profile.setWeixinBound(Boolean.TRUE);
            if (null != weixinUser && StringUtils.isNotBlank(weixinUser.getWeixinName())) {
                profile.setWeixinName(weixinUser.getWeixinName());
            }

            String token = JwtUtil.generateToken(profile.getUsername());
            return LoginResponseDTO.builder()
                    .token(token)
                    .expiresAt(jwtUtil.extractExpirationTime(token))
                    .user(profile)
                    .build();
        }

        return ThirdPartyPendingLoginResponseDTO.builder()
                .status("PENDING_DECISION")
                .pendingToken(ticket)
                .authType(AuthType.WECHAT.getCode())
                .authKey(openId)
                .displayName(null == weixinUser ? "" : weixinUser.getWeixinName())
                .avatar(null == weixinUser ? "" : weixinUser.getWeixinImageUrl())
                .weixinName(null == weixinUser ? "" : weixinUser.getWeixinName())
                .build();
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

    @Override
    public LoginResponseDTO bindExistingAccount(BindExistingAccountRequestDTO request) {
        String openId = loginReposity.checkLogin(request.getPendingToken());
        if (StringUtils.isBlank(openId)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "无效的微信登录状态");
        }

        if (null != loginReposity.queryUserIdByAuthTypeAndAuthKey(AuthType.WECHAT.getCode(), openId)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "微信账号已绑定其他用户");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        if (null == authentication) {
            throw new AppException(ResponseCode.LOGIN_ERROR);
        }

        CurrentUserResponseDTO profile = loginReposity.queryCurrentUser(request.getUsername());
        loginReposity.bindAuthToUser(profile.getId(), AuthType.WECHAT.getCode(), openId);

        WeixinUserEntity weixinUser = loginReposity.queryWeixinUserByOpenId(openId);
        profile.setLoginType(AuthType.WECHAT.getCode());
        profile.setWeixinBound(Boolean.TRUE);
        if (null != weixinUser && StringUtils.isNotBlank(weixinUser.getWeixinName())) {
            profile.setWeixinName(weixinUser.getWeixinName());
        }

        String token = JwtUtil.generateToken(profile.getUsername());
        return LoginResponseDTO.builder()
                .token(token)
                .expiresAt(jwtUtil.extractExpirationTime(token))
                .user(profile)
                .build();
    }

    @Override
    public LoginResponseDTO createAccount(CreateThirdPartyAccountRequestDTO request) {
        String openId = loginReposity.checkLogin(request.getPendingToken());
        if (StringUtils.isBlank(openId)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "无效的微信登录状态");
        }

        if (null != loginReposity.queryUserIdByAuthTypeAndAuthKey(AuthType.WECHAT.getCode(), openId)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "微信账号已绑定其他用户");
        }

        WeixinUserEntity weixinUser = loginReposity.queryWeixinUserByOpenId(openId);
        CurrentUserResponseDTO profile = loginReposity.createUserForThirdParty(
                AuthType.WECHAT.getCode(),
                openId,
                null == weixinUser ? "" : weixinUser.getWeixinName(),
                null == weixinUser ? "" : weixinUser.getWeixinImageUrl()
        );
        profile.setLoginType(AuthType.WECHAT.getCode());
        profile.setWeixinBound(Boolean.TRUE);
        if (null != weixinUser && StringUtils.isNotBlank(weixinUser.getWeixinName())) {
            profile.setWeixinName(weixinUser.getWeixinName());
        }

        String token = JwtUtil.generateToken(profile.getUsername());
        return LoginResponseDTO.builder()
                .token(token)
                .expiresAt(jwtUtil.extractExpirationTime(token))
                .user(profile)
                .build();
    }

    @Override
    public String createBindQrCodeTicket() throws IOException {
        return loginPort.createQrCodeTicket();
    }

    @Override
    public CurrentUserResponseDTO checkBind(String ticket, String username) {
        String openId = loginReposity.checkLogin(ticket);
        if (StringUtils.isBlank(openId)) {
            return null;
        }

        CurrentUserResponseDTO currentUser = loginReposity.queryCurrentUser(username);
        Long boundUserId = loginReposity.queryUserIdByAuthTypeAndAuthKey(AuthType.WECHAT.getCode(), openId);
        if (null != boundUserId && !boundUserId.equals(currentUser.getId())) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "微信账号已绑定其他用户");
        }

        if (null == boundUserId) {
            loginReposity.bindAuthToUser(currentUser.getId(), AuthType.WECHAT.getCode(), openId);
        }

        CurrentUserResponseDTO profile = loginReposity.queryCurrentUser(username);
        profile.setWeixinBound(Boolean.TRUE);
        return profile;
    }

    @Override
    public void unbindWechat(String username) {
        CurrentUserResponseDTO currentUser = loginReposity.queryCurrentUser(username);
        loginReposity.unbindAuthFromUser(currentUser.getId(), AuthType.WECHAT.getCode());
    }
}
