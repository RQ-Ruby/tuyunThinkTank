package com.RQ.tuyunthinktank.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.RQ.tuyunthinktank.exception.ErrorCode;
import com.RQ.tuyunthinktank.exception.ThrowUtils;
import com.RQ.tuyunthinktank.manage.FileManage;
import com.RQ.tuyunthinktank.model.dto.file.UploadPictureResult;
import com.RQ.tuyunthinktank.model.dto.picture.PictureQueryRequest;
import com.RQ.tuyunthinktank.model.dto.picture.PictureReviewRequest;
import com.RQ.tuyunthinktank.model.dto.picture.PictureUploadRequest;
import com.RQ.tuyunthinktank.model.entity.User;
import com.RQ.tuyunthinktank.model.enums.PictureReviewStatusEnum;
import com.RQ.tuyunthinktank.model.vo.PictureVO;
import com.RQ.tuyunthinktank.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.RQ.tuyunthinktank.model.entity.Picture;
import com.RQ.tuyunthinktank.service.PictureService;
import com.RQ.tuyunthinktank.mapper.PictureMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.hutool.core.date.DateUtil.date;

/**
 * @author RQ
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2025-06-08 21:53:08
 */
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {

    @Resource
    private FileManage fileManage;
    @Resource
    private UserService userService;

    /**
     * @description 校验图片信息
     * @author RQ
     * @date 2025/6/13 下午3:52
     */
    public void validPicture(Picture picture) {
        // 1. 基础空值校验
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR, "图片信息为空");

        // 2. 关键字段存在性校验
        Long id = picture.getId();
        String introduction = picture.getIntroduction();
        String url = picture.getUrl();

        // 3. 更新操作校验规则（ID必须存在）
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "更新操作需要图片ID");

        // 4. 内容长度校验（与数据库字段长度约束保持一致）
        // 简介最大支持8192字符（8KB文本）
        ThrowUtils.throwIf(StrUtil.isNotBlank(introduction) && introduction.length() > 8192,
                ErrorCode.PARAMS_ERROR, "简介长度超过8KB限制");

        // 5. URL格式校验（2048字符适配主流浏览器URL长度限制）
        ThrowUtils.throwIf(StrUtil.isNotBlank(url) && url.length() > 2048,
                ErrorCode.PARAMS_ERROR, "图片地址超过2048字符限制");
    }


/**
 * @description 上传图片
 * @author RQ
 * @date 2025/6/13 下午3:51
 */
    @Override
    public PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser) {
        //1.判断用户是否登录
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        //2.初始化id,用于判断是更新还是新增
        Long id = null;
        if (pictureUploadRequest.getId() != null) {
            id = pictureUploadRequest.getId();
        }
        //3.如果是更新，判断是否存在该图片
        if (id != null) {
            boolean exists = this.lambdaQuery()
                    .eq(Picture::getId, id)
                    .exists();
            ThrowUtils.throwIf(!exists, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        }
        //4.按照用户id划分目录
        String originalFilename = String.format("public/%s", loginUser.getId());
        UploadPictureResult uploadPictureResult = fileManage.uploadFile(multipartFile, originalFilename);

        //5.构造要入库的图片信息
        Picture picture = getPic(loginUser, uploadPictureResult, id);
        //6.设置审核状态
        setPictureReviewStatus(picture, loginUser);
        boolean result = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败");
        return PictureVO.objToVo(picture);


    }

    /**
     * @description 构造要入库的图片信息
     * @author RQ
     * @date 2025/6/12 下午3:22
     */
    private static Picture getPic(User loginUser, UploadPictureResult uploadPictureResult, Long id) {
        Picture picture = new Picture();
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setName(uploadPictureResult.getPicName());
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());
        //6.如果 pictureId 不为空，表示更新，否则是新增
        if (id != null) {
            // 如果是更新，需要补充 id 和编辑时间
            picture.setId(id);
            picture.setEditTime(new Date());
        }
        return picture;
    }

    /**
     * @description 分页查询
     * @author RQ
     * @date 2025/6/13 下午3:19
     */
    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (pictureQueryRequest == null) {
            return queryWrapper;
        }
        // 构建查询条件（ 根据 reviewStatus、reviewMessage 和 reviewerId 进行查询）
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);

        // 从请求对象中提取查询参数
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();

        // 多字段联合搜索：在名称和简介字段中进行模糊查询
        if (StrUtil.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("name", searchText)
                    .or()
                    .like("introduction", searchText));
        }

        // 添加精确匹配条件
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);  // 图片ID精确匹配
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);  // 用户ID精确匹配

        // 添加模糊匹配条件
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);  // 图片名称模糊查询
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);  // 简介模糊查询
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);  // 图片格式模糊查询

        // 分类精确匹配
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);

        // 图片属性精确匹配
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);  // 图片宽度
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);  // 图片高度
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);  // 图片大小（字节）
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);  // 图片比例

        // 处理标签数组查询（JSON数组存储方式）
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                // 使用JSON格式匹配标签
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }

        // 添加排序条件（支持升序/降序）
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField),
                sortOrder.equals("ascend"),  // 判断排序方向
                sortField);  // 排序字段

        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }
/**
 * @description  对象转封装类,只对单条数据进行脱敏
 * @author RQ
 * @date 2025/6/13 下午3:28
 */
    @Override
    public PictureVO getPictureVO(Picture picture,  HttpServletRequest request) {
        PictureVO pictureVO = PictureVO.objToVo(picture);
        // 关联用户信息（通过图片实体的 userId 字段）
        Long userId = picture.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            ThrowUtils.throwIf(user == null, ErrorCode.OPERATION_ERROR, "关联用户不存在");
            pictureVO.setUser(userService.getUserVO(user));
        }

        return pictureVO;
    }
   /**
    * @description 分页对象转封装类
    * @author RQ
    * @date 2025/6/13 下午5:21
    */
    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
        // 对象列表 => 封装对象列表
        List<PictureVO> pictureVOList = pictureList.stream().map(PictureVO::objToVo).collect(Collectors.toList());
        // 1. 关联查询用户信息
        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVO.setUser(userService.getUserVO(user));
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }
/**
 * @description 图片审核
 * @author RQ
 * @date 2025/7/8 上午10:01
 */
    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        //1.判断数据是否存在
        Long id = pictureReviewRequest.getId();
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "图片ID为空");
        //状态
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        PictureReviewStatusEnum enumByValue = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        ThrowUtils.throwIf(enumByValue == null, ErrorCode.PARAMS_ERROR, "状态不存在");
        //审核信息
        String reviewMessage = pictureReviewRequest.getReviewMessage();
        ThrowUtils.throwIf(StrUtil.isNotBlank(reviewMessage) && reviewMessage.length() > 8192, ErrorCode.PARAMS_ERROR, "审核信息过长");
        //审核信息不能为空
        ThrowUtils.throwIf(StrUtil.isBlank(reviewMessage), ErrorCode.PARAMS_ERROR, "审核信息不能为空");
        //3.通过id判断图片是否存在
        Picture oldpicture = this.getById(id);
        ThrowUtils.throwIf(oldpicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        //2.判断是否重复上传
        if(oldpicture.getReviewStatus().equals(reviewStatus)){
            ThrowUtils.throwIf(!oldpicture.getReviewMessage().equals(reviewMessage), ErrorCode.PARAMS_ERROR, "请勿重复审核");
        }
        //4.更新数据
        Picture picture = new Picture();
        BeanUtils.copyProperties(oldpicture, picture);
        picture.setUserId(loginUser.getId());
        picture.setUpdateTime(date());
        boolean b = this.updateById(picture);
            ThrowUtils.throwIf(!b, ErrorCode.OPERATION_ERROR, "审核失败");

    }
/**
 * @description 设置审核状态
 * @author RQ
 * @date 2025/7/9 下午2:03
 */
    @Override
    public void setPictureReviewStatus(Picture picture, User loginUser) {
        //管理员自动过审
       if(userService.isAdmin(loginUser)){
           picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
           picture.setReviewMessage("管理员自动过审");
       }
    else {
           picture.setReviewStatus(PictureReviewStatusEnum. REVIEWING.getValue());

       }
    }

}




