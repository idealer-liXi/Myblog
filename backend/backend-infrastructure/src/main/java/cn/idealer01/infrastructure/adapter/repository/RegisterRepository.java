package cn.idealer01.infrastructure.adapter.repository;

import cn.idealer01.domain.auth.adapter.repository.IRegisterRepository;
import cn.idealer01.infrastructure.dao.IUserDao;
import cn.idealer01.infrastructure.dao.IUserLevelDao;
import cn.idealer01.infrastructure.dao.po.User;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class RegisterRepository implements IRegisterRepository {
    @Resource
    private IUserDao userDao;

    @Override
    public void insertRegularUser(String username, String password) {
        //1.检查用户是否已经存在
        User userByUserName = userDao.getUserByUserName(username);
        if(null != userByUserName){
            throw new AppException(ResponseCode.USER_EXIST);
        }

        //2.不存在，插入数据
        userDao.insertUser(User.builder()
                        .username(username)
                        .password(password)
                .build());
    }
}
