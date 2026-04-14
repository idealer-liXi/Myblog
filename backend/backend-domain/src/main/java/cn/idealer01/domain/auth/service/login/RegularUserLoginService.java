package cn.idealer01.domain.auth.service.login;

import cn.idealer01.domain.auth.model.aggregate.LoginUserAggregate;
import cn.idealer01.domain.auth.service.IRegularUserLoginService;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import cn.idealer01.types.utils.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

@Service
public class RegularUserLoginService implements IRegularUserLoginService {
    /**
     * 已在SecurityConfig中完成了对AuthenticationManager的配置，即配置了UserDetailService以及PasswordEncoder
     */
    @Resource
    private AuthenticationManager authenticationManager;

    @Override
    public String getToken(String username, String password) {
        // 把前端传来的username和password整合成token，此时的authenticationToken处于未认证状态
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);

        //对这个token交付给SpringSecurity进行认证校验，验证这个人是谁、密码对不对、是否有权限， 返回得到认证的authenticate
        /**
         *  SecurityConfig中配置的AuthenticationManager（封装上PasswordEncoder和 UserDetailsService）
         *  底层调用的是UserDetailService的loadUserByUsername方法判断用户账号密码是否正确
         *  返回将返回的UserDetails封装成Authentication
         */
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);

        //若认证为空，则说明用户登录失败，与在数据库中查询到username和password不符
        if(Objects.isNull(authenticate)){
            throw new AppException(ResponseCode.LOGIN_ERROR);
        }

        LoginUserAggregate loginUser = (LoginUserAggregate) authenticate.getPrincipal();

        return JwtUtil.generateToken(loginUser.getUsername());
    }
}
