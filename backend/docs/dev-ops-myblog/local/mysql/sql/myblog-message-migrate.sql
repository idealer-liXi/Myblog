/*
  为已初始化的 myblog 数据库补充留言板相关表。
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `myblog` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `myblog`;

CREATE TABLE IF NOT EXISTS `message` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) unsigned NOT NULL COMMENT '留言用户ID',
  `username` varchar(64) NOT NULL DEFAULT '' COMMENT '留言用户名快照',
  `content` varchar(500) NOT NULL DEFAULT '' COMMENT '留言内容',
  `status` varchar(16) NOT NULL DEFAULT 'PUBLISHED' COMMENT '留言状态',
  `sentiment` varchar(16) NOT NULL DEFAULT 'UNKNOWN' COMMENT '情绪标签',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_message_user_id` (`user_id`),
  KEY `idx_message_status_time` (`status`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='留言表';

CREATE TABLE IF NOT EXISTS `message_settings` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `review_mode` varchar(32) NOT NULL DEFAULT 'AUTO_PUBLISH' COMMENT '发布模式 AUTO_PUBLISH/MANUAL_REVIEW',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='留言板配置表';

INSERT INTO `message_settings` (`review_mode`)
SELECT 'AUTO_PUBLISH'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `message_settings`
);

SET FOREIGN_KEY_CHECKS = 1;
