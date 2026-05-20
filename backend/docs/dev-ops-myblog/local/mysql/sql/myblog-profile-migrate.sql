/*
  为已初始化的 myblog 数据库补充站主介绍管理相关表，并写入默认信息。
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `myblog` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `myblog`;

CREATE TABLE IF NOT EXISTS `profile_settings` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `avatar` varchar(512) NOT NULL DEFAULT '' COMMENT '头像地址',
  `name` varchar(128) NOT NULL DEFAULT '' COMMENT '姓名',
  `bio` varchar(255) NOT NULL DEFAULT '' COMMENT '职业描述',
  `email` varchar(128) NOT NULL DEFAULT '' COMMENT '邮箱',
  `github_name` varchar(128) NOT NULL DEFAULT '' COMMENT 'GitHub 名称',
  `github_url` varchar(512) NOT NULL DEFAULT '' COMMENT 'GitHub 地址',
  `location` varchar(128) NOT NULL DEFAULT '' COMMENT '地点',
  `introduction` text NOT NULL COMMENT '个人简介',
  `hobbies` text NOT NULL COMMENT '兴趣爱好换行文本',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站主介绍配置表';

CREATE TABLE IF NOT EXISTS `profile_school_honors` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `school_key` varchar(32) NOT NULL DEFAULT '' COMMENT '学校键 undergraduate/graduate',
  `honors` text NOT NULL COMMENT '荣誉换行文本',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_school_key` (`school_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站主学校荣誉配置表';

INSERT INTO `profile_settings` (`avatar`, `name`, `bio`, `email`, `github_name`, `github_url`, `location`, `introduction`, `hobbies`)
SELECT '@/assets/images/avatar.jpg',
       'Idealer',
       'Java开发工程师 | 大模型应用开发工程师 | 大模型算法工程师',
       '2755027635@qq.com',
       'idealer-liXi',
       'https://github.com/idealer-liXi',
       '辽宁-沈阳',
       '具备扎实的前端、后端开发能力，熟悉Vue、SpringBoot等主流框架，热爱开源和技术分享。喜欢设计美观、体验流畅的Web应用，追求代码的优雅与高效。乐于团队协作，善于沟通，持续学习新技术。',
       '编程与算法\n开源社区\n技术分享\n音乐与吉他\n美食探索'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `profile_settings`
);

INSERT INTO `profile_school_honors` (`school_key`, `honors`)
VALUES
  ('undergraduate', '国家级大学生创新创业训练计划\n全国大学生数学建模竞赛一等奖'),
  ('graduate', '暂无~~')
ON DUPLICATE KEY UPDATE honors = values(honors);

SET FOREIGN_KEY_CHECKS = 1;
