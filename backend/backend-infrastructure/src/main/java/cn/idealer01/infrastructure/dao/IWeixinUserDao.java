package cn.idealer01.infrastructure.dao;

import cn.idealer01.infrastructure.dao.po.WeixinUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IWeixinUserDao {

    WeixinUser queryWeixinUserByOpenId(String openid);

    void addWeixinUser(WeixinUser weixinUser);
}
