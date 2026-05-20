package cn.idealer01.domain.auth.adapter.repository;

import cn.idealer01.api.dto.CurrentUserResponseDTO;
import cn.idealer01.domain.auth.model.aggregate.LoginUserAggregate;
import cn.idealer01.domain.auth.model.entity.WeixinUserEntity;

import java.util.List;

public interface ILoginRepository {

    String checkLogin(String ticket);

    void saveLoginState(String ticket, String openId);

    WeixinUserEntity queryWeixinUserByOpenId(String openid);

    void addWeixinUser(String openid);

    LoginUserAggregate getRegularUserByUserName(String username);

    CurrentUserResponseDTO queryCurrentUser(String username);

    CurrentUserResponseDTO queryCurrentUserByUserId(Long userId);

    List<CurrentUserResponseDTO> queryAllCurrentUsers();

    Long queryUserIdByAuthTypeAndAuthKey(String authType, String authKey);

    void bindAuthToUser(Long userId, String authType, String authKey);

    CurrentUserResponseDTO createUserForThirdParty(String authType, String authKey, String displayName, String avatar);

    void unbindAuthFromUser(Long userId, String authType);

    void updateUserAvatar(Long userId, String avatar, String avatarSource);

    void updateUserDisplayName(Long userId, String displayName);

    void updateUserStatus(Long userId, Integer status);
}
