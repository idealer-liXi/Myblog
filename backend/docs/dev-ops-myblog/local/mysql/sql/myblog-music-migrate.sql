/*
  为已初始化的 myblog 数据库补充 music 表。

  适用场景：
  - 当前数据库已经存在 theme / article / image_record / project 等表
  - 现在只想增量补上 music 表

  注意：
  - 该脚本不会删除任何已有表
  - 若 music 表已经存在，则本脚本不会重复创建
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `myblog` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `myblog`;

CREATE TABLE IF NOT EXISTS `music` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(128) NOT NULL DEFAULT '' COMMENT '歌曲名',
  `artist` varchar(128) NOT NULL DEFAULT '' COMMENT '歌手',
  `audio_url` varchar(512) NOT NULL DEFAULT '' COMMENT '音频 OSS 地址',
  `cover_image` varchar(512) NOT NULL DEFAULT '' COMMENT '封面 OSS 地址',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '排序权重',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_music_enabled_sort` (`enabled`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='音乐表';

SET FOREIGN_KEY_CHECKS = 1;
