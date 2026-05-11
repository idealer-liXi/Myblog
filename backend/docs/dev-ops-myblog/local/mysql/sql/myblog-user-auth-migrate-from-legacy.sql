/*
  将旧版 myblog 用户表结构迁移到当前 user + user_auth + role + user_role 模型。

  适用场景：
  - 代码已经切到 display_name / avatar / user_auth / role / user_role
  - 当前运行中的 MySQL 仍然是旧表结构（例如 user 只有 username/password/level_id/status）

  注意：
  - 该脚本用于“已初始化过的旧库升级”
  - docker-entrypoint-initdb.d 只会在数据库首次初始化时自动执行
  - 如果 mysql 容器里的数据库已经存在，这个脚本需要手动执行，或者删除旧容器重建数据库
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `myblog` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `myblog`;

-- 1) 兼容“全新初始化”场景：若 user 表不存在则直接创建新结构
CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(64) DEFAULT NULL COMMENT '系统用户名，可用于展示或账号标识',
  `display_name` varchar(64) NOT NULL DEFAULT '' COMMENT '显示昵称',
  `avatar` varchar(255) NOT NULL DEFAULT '/images/default-avatar.png' COMMENT '头像地址',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '账号状态 0-禁用 1-正常',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统主用户表';

-- 2) 兼容“旧库升级”场景：给旧 user 表补齐新字段
SET @has_display_name_column := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user'
      AND COLUMN_NAME = 'display_name'
);

SET @add_display_name_sql := IF(
    @has_display_name_column = 0,
    'ALTER TABLE `user` ADD COLUMN `display_name` varchar(64) NOT NULL DEFAULT '''' COMMENT ''显示昵称'' AFTER `username`',
    'SELECT 1'
);

PREPARE stmt_add_display_name FROM @add_display_name_sql;
EXECUTE stmt_add_display_name;
DEALLOCATE PREPARE stmt_add_display_name;

SET @has_avatar_column := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user'
      AND COLUMN_NAME = 'avatar'
);

SET @add_avatar_sql := IF(
    @has_avatar_column = 0,
    'ALTER TABLE `user` ADD COLUMN `avatar` varchar(255) NOT NULL DEFAULT ''/images/default-avatar.png'' COMMENT ''头像地址'' AFTER `display_name`',
    'SELECT 1'
);

PREPARE stmt_add_avatar FROM @add_avatar_sql;
EXECUTE stmt_add_avatar;
DEALLOCATE PREPARE stmt_add_avatar;

-- 3) 让旧数据拥有基础 display_name / avatar
UPDATE `user`
SET `display_name` = `username`
WHERE (`display_name` IS NULL OR `display_name` = '')
  AND `username` IS NOT NULL;

UPDATE `user`
SET `avatar` = '/images/default-avatar.png'
WHERE `avatar` IS NULL OR `avatar` = '';

-- 4) 为全新初始化场景创建默认管理员账号
INSERT INTO `user` (`username`, `display_name`, `avatar`, `status`)
SELECT 'admin', '管理员', '/images/default-avatar.png', 1
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `user` WHERE `username` = 'admin'
);

-- 5) 创建 role 表
CREATE TABLE IF NOT EXISTS `role` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `role_code` varchar(32) NOT NULL DEFAULT '' COMMENT '角色编码',
  `role_name` varchar(32) NOT NULL DEFAULT '' COMMENT '角色名称',
  `description` varchar(128) NOT NULL DEFAULT '' COMMENT '角色描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`),
  UNIQUE KEY `uk_role_name` (`role_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

INSERT INTO `role` (`id`, `role_code`, `role_name`, `description`)
VALUES
  (1, 'USER', '普通用户', '默认前台用户'),
  (2, 'ADMIN', '后台管理员', '可访问后台管理功能')
ON DUPLICATE KEY UPDATE
  `role_name` = VALUES(`role_name`),
  `description` = VALUES(`description`);

-- 6) 创建 user_role 表
CREATE TABLE IF NOT EXISTS `user_role` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) unsigned NOT NULL COMMENT '用户ID',
  `role_id` bigint(20) unsigned NOT NULL COMMENT '角色ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id_role_id` (`user_id`, `role_id`),
  KEY `idx_role_id` (`role_id`),
  CONSTRAINT `fk_user_role_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_user_role_role_id` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 7) 为旧用户回填角色
-- admin 默认 ADMIN，其他用户默认 USER
INSERT INTO `user_role` (`user_id`, `role_id`)
SELECT u.id,
       CASE WHEN u.username = 'admin' THEN 2 ELSE 1 END AS role_id
FROM `user` u
LEFT JOIN `user_role` ur ON ur.user_id = u.id
WHERE ur.id IS NULL;

-- 8) 创建 user_auth 表
CREATE TABLE IF NOT EXISTS `user_auth` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) unsigned NOT NULL COMMENT '关联用户ID',
  `auth_type` varchar(32) NOT NULL DEFAULT '' COMMENT '登录方式：password/wechat/email',
  `auth_key` varchar(128) NOT NULL DEFAULT '' COMMENT '登录标识：用户名/openid/邮箱',
  `credential` varchar(255) DEFAULT NULL COMMENT '认证凭证：密码哈希；第三方登录可为空',
  `verified` tinyint(4) NOT NULL DEFAULT 1 COMMENT '是否已验证 0-未验证 1-已验证',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_auth_type_auth_key` (`auth_type`, `auth_key`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_auth_type_user_id` (`auth_type`, `user_id`),
  CONSTRAINT `fk_user_auth_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户多登录方式绑定表';

-- 9) 仅当旧 user.password 字段存在时，回填 password 类型认证记录
SET @has_password_column := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user'
      AND COLUMN_NAME = 'password'
);

SET @backfill_user_auth_sql := IF(
    @has_password_column > 0,
    'INSERT INTO user_auth (user_id, auth_type, auth_key, credential, verified) SELECT u.id, ''password'', u.username, u.password, 1 FROM user u LEFT JOIN user_auth ua ON ua.auth_type = ''password'' AND ua.auth_key = u.username WHERE ua.id IS NULL AND u.username IS NOT NULL AND u.password IS NOT NULL',
    'SELECT 1'
);

PREPARE stmt_user_auth FROM @backfill_user_auth_sql;
EXECUTE stmt_user_auth;
DEALLOCATE PREPARE stmt_user_auth;

-- 10) 全新初始化时，旧 user.password 字段不存在，补一条默认管理员 password 认证记录
INSERT INTO `user_auth` (`user_id`, `auth_type`, `auth_key`, `credential`, `verified`)
SELECT u.id, 'password', 'admin', '$2a$10$7EqJtq98hPqEX7fNZaFWoOePaWxn96p36sJzP6VSZ/Ik2Vq6KYFQe', 1
FROM `user` u
WHERE u.username = 'admin'
  AND NOT EXISTS (
      SELECT 1
      FROM `user_auth` ua
      WHERE ua.auth_type = 'password'
        AND ua.auth_key = 'admin'
  );

SET FOREIGN_KEY_CHECKS = 1;
