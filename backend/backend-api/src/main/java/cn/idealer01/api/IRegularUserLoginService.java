package cn.idealer01.api;

import cn.idealer01.api.response.Response;

public interface IRegularUserLoginService extends ILoginService{

    Response<String> register();

    Response<String> login();

}
