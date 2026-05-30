package com.RQ.tuyunthinktank.service.impl;

import com.RQ.tuyunthinktank.mapper.PictureFavoriteMapper;
import com.RQ.tuyunthinktank.model.entity.PictureFavorite;
import com.RQ.tuyunthinktank.service.PictureFavoriteService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 图片收藏服务实现
 */
@Service
public class PictureFavoriteServiceImpl extends ServiceImpl<PictureFavoriteMapper, PictureFavorite>
        implements PictureFavoriteService {

    @Override
    public boolean addFavorite(Long pictureId, Long userId) {
        QueryWrapper<PictureFavorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("pictureId", pictureId);
        queryWrapper.eq("userId", userId);
        
        PictureFavorite existingFavorite = this.getOne(queryWrapper);
        if (existingFavorite != null) {
            return true;
        }
        
        PictureFavorite favorite = new PictureFavorite();
        favorite.setPictureId(pictureId);
        favorite.setUserId(userId);
        return this.save(favorite);
    }

    @Override
    public boolean removeFavorite(Long pictureId, Long userId) {
        QueryWrapper<PictureFavorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("pictureId", pictureId);
        queryWrapper.eq("userId", userId);
        return this.remove(queryWrapper);
    }

    @Override
    public boolean isFavorite(Long pictureId, Long userId) {
        QueryWrapper<PictureFavorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("pictureId", pictureId);
        queryWrapper.eq("userId", userId);
        return this.count(queryWrapper) > 0;
    }
}