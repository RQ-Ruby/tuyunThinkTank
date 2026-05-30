package com.RQ.tuyunthinktank.service;

import com.RQ.tuyunthinktank.model.entity.PictureFavorite;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 图片收藏服务
 */
public interface PictureFavoriteService extends IService<PictureFavorite> {

    /**
     * 收藏图片
     */
    boolean addFavorite(Long pictureId, Long userId);

    /**
     * 取消收藏
     */
    boolean removeFavorite(Long pictureId, Long userId);

    /**
     * 检查是否已收藏
     */
    boolean isFavorite(Long pictureId, Long userId);
}