package cn.idealer01.domain.auth.service;

import cn.idealer01.api.dto.CurrentUserResponseDTO;
import cn.idealer01.domain.auth.adapter.repository.ILoginRepository;
import cn.idealer01.domain.auth.model.aggregate.LoginUserAggregate;
import cn.idealer01.domain.auth.model.entity.UserEntity;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class UserDetailsServiceImpl implements UserDetailsService {
    @Resource
    private ILoginRepository loginReposity;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            return loginReposity.getRegularUserByUserName(username);
        } catch (AppException exception) {
            if (!ResponseCode.LOGIN_ERROR.getCode().equals(exception.getCode())) {
                throw exception;
            }

            CurrentUserResponseDTO currentUser = loginReposity.queryCurrentUser(username);
            List<String> roles = currentUser.getRoles();
            String roleCode = null == roles || roles.isEmpty() ? null : roles.get(0);
            if (StringUtils.isBlank(roleCode)) {
                throw new AppException(ResponseCode.ROLE_NOT_EXIST);
            }

            return LoginUserAggregate.builder()
                    .userEntity(UserEntity.builder()
                            .username(currentUser.getUsername())
                            .password("")
                            .roleCode(roleCode)
                            .status(1)
                            .build())
                    .build();
        }
    }
}
