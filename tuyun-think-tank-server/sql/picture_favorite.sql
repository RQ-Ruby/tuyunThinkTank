-- 图片收藏表
CREATE TABLE `picture_favorite` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `pictureId` bigint NOT NULL COMMENT '图片 id',
  `userId` bigint NOT NULL COMMENT '用户 id',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_userId_pictureId` (`userId`, `pictureId`),
  KEY `idx_userId` (`userId`),
  KEY `idx_pictureId` (`pictureId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图片收藏';