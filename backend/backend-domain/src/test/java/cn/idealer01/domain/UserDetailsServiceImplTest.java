package cn.idealer01.domain;

import cn.idealer01.api.dto.CurrentUserResponseDTO;
import cn.idealer01.domain.auth.adapter.repository.ILoginRepository;
import cn.idealer01.domain.auth.service.UserDetailsServiceImpl;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest {

    @Mock
    private ILoginRepository loginRepository;

    @Test
    public void loadUserByUsername_shouldAllowWechatOnlyUserWithoutPasswordCredential() {
        UserDetailsServiceImpl service = new UserDetailsServiceImpl();
        ReflectionTestUtils.setField(service, "loginReposity", loginRepository);

        when(loginRepository.getRegularUserByUserName("wx_only_user"))
                .thenThrow(new AppException(ResponseCode.LOGIN_ERROR));
        when(loginRepository.queryCurrentUser("wx_only_user"))
                .thenReturn(CurrentUserResponseDTO.builder()
                        .username("wx_only_user")
                        .roles(Collections.singletonList("USER"))
                        .build());

        UserDetails userDetails = service.loadUserByUsername("wx_only_user");

        Assertions.assertEquals("wx_only_user", userDetails.getUsername());
        Assertions.assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_USER".equals(authority.getAuthority())));
    }
}
