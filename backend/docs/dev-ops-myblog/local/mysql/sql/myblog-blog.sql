/*
 MyBlog 博客内容建表脚本

 包含：
 - theme     : 主题管理表
 - article   : 文章表
 - image_record : 图片记录表
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `myblog` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `myblog`;

-- ----------------------------
-- Table structure for theme
-- ----------------------------
DROP TABLE IF EXISTS `theme`;
CREATE TABLE `theme` (
  `id`          bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name`        varchar(64)  NOT NULL DEFAULT ''       COMMENT '主题名称',
  `icon`        varchar(128) NOT NULL DEFAULT ''       COMMENT '图标类名，如 bi bi-cup-hot-fill',
  `color`       varchar(16)  NOT NULL DEFAULT '#007bff' COMMENT '主题色',
  `sort_order`  int(11)      NOT NULL DEFAULT 0        COMMENT '排序权重',
  `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='主题管理表';

INSERT INTO `theme` (`name`, `icon`, `color`, `sort_order`) VALUES
('java', 'bi bi-cup-hot-fill', '#e76f00', 1),
('python', 'bi bi-stack', '#3776ab', 2),
('c++', 'bi bi-cpu-fill', '#00599c', 3),
('vue', 'bi bi-palette2', '#42b883', 4);

-- ----------------------------
-- Table structure for article
-- ----------------------------
DROP TABLE IF EXISTS `article`;
CREATE TABLE `article` (
  `id`          bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `title`       varchar(256) NOT NULL DEFAULT ''       COMMENT '文章标题',
  `content`     longtext     NOT NULL                  COMMENT '文章内容（Markdown）',
  `summary`     varchar(512) NOT NULL DEFAULT ''       COMMENT '文章摘要',
  `theme`       varchar(64)  NOT NULL DEFAULT ''       COMMENT '主题标签',
  `cover_image` varchar(512) NOT NULL DEFAULT ''       COMMENT '封面图URL',
  `status`      varchar(16)  NOT NULL DEFAULT 'DRAFT'  COMMENT '状态：DRAFT / PUBLISHED',
  `author_id`   bigint(20) unsigned NOT NULL           COMMENT '作者ID，关联 user.id',
  `view_count`  int(10) unsigned NOT NULL DEFAULT 0    COMMENT '浏览次数',
  `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_author`   (`author_id`),
  KEY `idx_status`   (`status`),
  KEY `idx_theme`    (`theme`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';

-- ----------------------------
-- Table structure for image_record
-- ----------------------------
DROP TABLE IF EXISTS `image_record`;
CREATE TABLE `image_record` (
  `id`          bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `file_name`   varchar(256) NOT NULL DEFAULT ''       COMMENT '原始文件名',
  `oss_url`     varchar(512) NOT NULL DEFAULT ''       COMMENT '阿里云OSS访问URL',
  `file_size`   bigint(20)   NOT NULL DEFAULT 0        COMMENT '文件大小（字节）',
  `uploader_id` bigint(20) unsigned NOT NULL           COMMENT '上传者ID，关联 user.id',
  `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_uploader` (`uploader_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图片记录表';

SET FOREIGN_KEY_CHECKS = 1;
