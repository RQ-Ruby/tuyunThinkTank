package com.RQ.tuyunthinktank.service;

import com.RQ.tuyunthinktank.model.dto.picture.PictureUploadRequest;
import com.RQ.tuyunthinktank.model.entity.Picture;
import com.RQ.tuyunthinktank.model.entity.User;
import com.RQ.tuyunthinktank.model.vo.PictureVO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

/**
* @author RQ
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-06-08 21:53:08
*/
public interface PictureService extends IService<Picture> {


    /**
     * 上传图片
     *
     * @param multipartFile
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(MultipartFile multipartFile,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);

}
