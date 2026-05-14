/*
 MyBlog 微信用户扩展资料表

 说明：
 - weixin_user 是微信侧资料缓存表，不是站内主账号表
 - 主键 openid 唯一，一个微信用户只对应一条资料记录
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `myblog` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `myblog`;

-- ----------------------------
-- Table structure for weixin_user
-- ----------------------------
DROP TABLE IF EXISTS `weixin_user`;
CREATE TABLE `weixin_user` (
  `id`               bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `openid`           varchar(128) NOT NULL DEFAULT ''       COMMENT '微信OpenID',
  `weixin_name`      varchar(64)  NOT NULL DEFAULT ''       COMMENT '微信昵称',
  `weixin_image_url` varchar(512) NOT NULL DEFAULT ''       COMMENT '微信头像URL',
  `create_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='微信用户扩展资料表';

SET FOREIGN_KEY_CHECKS = 1;
