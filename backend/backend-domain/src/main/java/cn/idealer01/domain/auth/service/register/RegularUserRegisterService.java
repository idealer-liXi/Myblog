package cn.idealer01.domain.auth.service.register;

import cn.idealer01.domain.auth.adapter.repository.IRegisterRepository;
import cn.idealer01.domain.auth.service.IRegularUserRegisterService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class RegularUserRegisterService implements IRegularUserRegisterService {
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private IRegisterRepository registerRepository;

    @Override
    public void register(String username, String password) {
        //1.todo 检查username和password是否符合规则
        //2.对密码进行加密
        String encode = passwordEncoder.encode(password);
        //2.将数据添加进数据库
        registerRepository.insertRegularUser(username, encode);
    }
}
