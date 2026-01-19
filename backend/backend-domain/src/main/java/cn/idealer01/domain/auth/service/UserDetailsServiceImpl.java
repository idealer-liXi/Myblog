package cn.idealer01.domain.auth.service;
import cn.idealer01.domain.auth.adapter.repository.ILoginRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class UserDetailsServiceImpl implements UserDetailsService {
    @Resource
    private ILoginRepository loginReposity;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return loginReposity.getRegularUserByUserName(username);
    }
}
