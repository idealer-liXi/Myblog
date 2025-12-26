package cn.idealer01.api;

import cn.idealer01.api.response.Response;

public interface ILoginService {
    //检查当前用户是否登录
    Response<String> checkLogin(String ticket);
}
