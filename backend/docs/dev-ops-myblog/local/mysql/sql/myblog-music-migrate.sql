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

INSERT IGNORE INTO `music` (`id`, `name`, `artist`, `audio_url`, `cover_image`, `sort_order`, `enabled`, `create_time`, `update_time`) VALUES
(1, '红色高跟鞋', '蔡健雅', 'https://idealer-blog.oss-cn-beijing.aliyuncs.com/blog-images/music/audio/eb27460d-1379-4cf1-a272-e09bb9f40412.flac', 'https://idealer-blog.oss-cn-beijing.aliyuncs.com/blog-images/music/cover/3391b3a7-b620-4e68-8e9a-8501b70e781b.jpg', 0, 1, '2026-05-20 16:44:51', '2026-05-20 16:44:51'),
(2, '起风了', '买辣椒也用劵', 'https://idealer-blog.oss-cn-beijing.aliyuncs.com/blog-images/music/audio/6231c7c6-340e-4db4-9f35-2d3d4e56214b.mp3', 'https://idealer-blog.oss-cn-beijing.aliyuncs.com/blog-images/music/cover/75d108ce-b589-45a9-8328-8e626e68a8dc.jpg', 0, 1, '2026-05-20 20:49:56', '2026-05-20 20:49:56');

SET FOREIGN_KEY_CHECKS = 1;