package com.RQ.tuyunthinktank.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import com.RQ.tuyunthinktank.common.LocalCacheManager;
import com.RQ.tuyunthinktank.exception.BusinessException;
import com.RQ.tuyunthinktank.exception.ErrorCode;
import com.RQ.tuyunthinktank.exception.ThrowUtils;
import com.RQ.tuyunthinktank.manage.FileManage;
import com.RQ.tuyunthinktank.manage.upload.FilePictureUpload;
import com.RQ.tuyunthinktank.manage.upload.UrlPictureUpload;
import com.RQ.tuyunthinktank.model.dto.file.UploadPictureResult;
import com.RQ.tuyunthinktank.model.dto.picture.*;
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
;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static cn.hutool.core.date.DateUtil.date;


/**
 * @author RQ
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2025-06-08 21:53:08
 */
@Slf4j
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {

    @Resource
    private FileManage fileManage;
    @Resource
    private UserService userService;
    @Resource
    private FilePictureUpload filePictureUpload;
    @Resource
    private UrlPictureUpload urlPictureUpload;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private LocalCacheManager localCacheManager;




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
    public PictureVO uploadPicture(Object multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser) {
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
        //5.上传图片
        UploadPictureResult uploadPictureResult = null;
        if (multipartFile instanceof MultipartFile) {
              uploadPictureResult=filePictureUpload.uploadPicture(multipartFile, originalFilename);
        } else if (multipartFile instanceof String) {
             uploadPictureResult =  urlPictureUpload.uploadPicture(multipartFile, originalFilename);
        } else {
            ThrowUtils.throwIf(true, ErrorCode.PARAMS_ERROR, "上传文件类型错误");
        }

        //6.构造要入库的图片信息
        Picture picture = getPic(loginUser, uploadPictureResult, id);

        String picName = uploadPictureResult.getPicName();
        if (StrUtil.isNotBlank(pictureUploadRequest.getPicName())) {
            picName = pictureUploadRequest.getPicName();
        }

        picture.setName(picName);
        //7.设置审核状态
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
        Picture updatpicture = new Picture();
        BeanUtils.copyProperties(pictureReviewRequest, updatpicture);
        updatpicture.setUserId(loginUser.getId());
        updatpicture.setUpdateTime(date());
        boolean b = this.updateById(updatpicture);
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
/**
 * @return
 * @description 批量抓取图片
 * @author RQ
 * @date 2025/7/17 上午9:14
 */
    @Override
    public int doPictureBatchUpload(PictureByBatchRequest pictureByBatchRequest, User loginUser) {
        //判断数据是否存在
        String searchText = pictureByBatchRequest.getSearchText();
        Integer count = pictureByBatchRequest.getCount();
        String picName = pictureByBatchRequest.getPicName();
        // 名称前缀默认是searchText
        if(StrUtil.isBlank(picName)){
            picName=searchText;
        }
        ThrowUtils.throwIf(StrUtil.isBlank(searchText), ErrorCode.PARAMS_ERROR, "搜索词不能为空");
        ThrowUtils.throwIf(count == null || count <= 0, ErrorCode.PARAMS_ERROR, "抓取数量不能为空");
       //抓取数量不能超过30
        ThrowUtils.throwIf(count > 30, ErrorCode.PARAMS_ERROR, "抓取数量不能超过30");
        //构建url
        String fetchUrl = "https://cn.bing.com/images/async?q="+searchText+"&mmasync=1";
        //进行链接
        Document document;
        try {
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            log.error("抓取图片失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取页面失败");
        }
        // 定位包含图片的容器div（通过CSS选择器）
        Element div = document.getElementsByClass("dgControl").first();
        if (ObjUtil.isNull(div)) {  // 空指针防护
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取元素失败");
        }

        // 提取所有图片元素（选择class="mimg"的img标签）
        Elements imgElementList = div.select("img.mimg");
        if (CollUtil.isEmpty(imgElementList)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取元素失败");
        }

        // 遍历图片元素，提取图片URL
        List<String> imgUrlList = new ArrayList<>();
        int uploadCount=0;
        for (Element imgElement : imgElementList) {
            String imgUrl = imgElement.attr("src");
            //判断url是否为空
            if(StrUtil.isBlank(imgUrl)){
                log.info("图片url为空,已跳过:{}",imgUrl);
                continue;
            }
            imgUrlList.add(imgUrl);
            //清楚url参数
            imgUrl = imgUrl.split("\\?")[0];

            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            try {
                pictureUploadRequest.setPicName(picName+uploadCount+1);
                PictureVO pictureVO = this.uploadPicture(imgUrl, pictureUploadRequest, loginUser);
                log.info("图片上传成功, id = {}", pictureVO.getId());
                uploadCount++;  // 成功计数
            } catch (Exception e) {
                log.error("图片上传失败", e);  // 捕获具体异常但不中断流程
                continue;
            }
            //判断是否上传成功
            if(uploadCount>=count){
                break;
            }

        }
        // 打印所有图片URL
        for (String imgUrl : imgUrlList) {
            System.out.println(imgUrl);
        }



        return uploadCount;  // 返回实际成功数量

    }
/**
 * @description 分页查询图片cache
 * @author RQ
 * @date 2025/7/19 下午5:28
 */
    @Override
    public Page<PictureVO> listPictureVOByPageWithCache(PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 普通用户默认只能查看已过审的数据
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());

        // 构建缓存 key
        String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
        String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
        String cacheKey = "yupicture:listPictureVOByPage:" + hashKey;


         // 1.先从性能高的本地缓存中查询
        String cachedValue = localCacheManager.getIfPresent(cacheKey);
        if (cachedValue != null) {
            // 如果缓存命中，返回结果
            Page<PictureVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
            return cachedPage;
        }

        // 2.再从 Redis 缓存中查询
        ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();
       cachedValue = valueOps.get(cacheKey);
        if (cachedValue != null) {
            // 如果缓存命中,更新本地缓存
            localCacheManager.put(cacheKey, cachedValue);
            // 如果缓存命中,返回结果
            Page<PictureVO> redisPage = JSONUtil.toBean(cachedValue, Page.class);
            return redisPage;
        }
        log.info("缓存未命中，开始查询数据库，cacheKey={}", cacheKey);
        // 3.查询数据库
        Page<Picture> picturePage = this.page(new Page<>(current, size),
                this.getQueryWrapper(pictureQueryRequest));
        // 获取封装类
        Page<PictureVO> pictureVOPage = this.getPictureVOPage(picturePage);
        // 存入本地缓存
        String cacheValue = JSONUtil.toJsonStr(pictureVOPage);
        localCacheManager.put(cacheKey, cacheValue);
        // 存入 Redis 缓存
        // 5 - 10 分钟随机过期，防止雪崩
        int cacheExpireTime = 300 +  RandomUtil.randomInt(0, 300);
        valueOps.set(cacheKey, cacheValue, cacheExpireTime, TimeUnit.SECONDS);

        // 返回结果
        return pictureVOPage;
    }

}




