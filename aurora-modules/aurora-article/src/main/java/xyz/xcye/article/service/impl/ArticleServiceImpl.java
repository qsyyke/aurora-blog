package xyz.xcye.article.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import xyz.xcye.article.dao.ArticleMapper;
import xyz.xcye.article.po.Article;
import xyz.xcye.article.po.Category;
import xyz.xcye.article.po.Tag;
import xyz.xcye.article.service.ArticleService;
import xyz.xcye.article.service.CategoryService;
import xyz.xcye.article.service.TagService;
import xyz.xcye.article.util.TimeUtils;
import xyz.xcye.article.vo.ArticleVO;
import xyz.xcye.aurora.properties.AuroraProperties;
import xyz.xcye.aurora.util.UserUtils;
import xyz.xcye.core.dto.JwtUserInfo;
import xyz.xcye.core.enums.ResponseStatusCodeEnum;
import xyz.xcye.core.exception.user.UserException;
import xyz.xcye.core.util.BeanUtils;
import xyz.xcye.core.util.DateUtils;
import xyz.xcye.core.util.id.GenerateInfoUtils;
import xyz.xcye.core.util.lambda.AssertUtils;
import xyz.xcye.data.entity.Condition;
import xyz.xcye.data.entity.PageData;
import xyz.xcye.data.util.PageUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author qsyyke
 * @date Created in 2022/5/11 17:14
 */

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private AuroraProperties auroraProperties;
    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private TagService tagService;
    @Autowired
    private CategoryService categoryService;

    @Override
    public int deleteByPrimaryKey(Long uid) {
        Assert.notNull(uid, "uid不能为null");

        // 构建一个Article对象
        Article article = Article.builder()
                .uid(uid)
                .delete(true)
                .build();
        // 逻辑删除
        return articleMapper.updateByPrimaryKeySelective(article);
    }

    @Override
    public int physicsDeleteByUid(Long uid) {
        Assert.notNull(uid, "uid不能为null");
        return articleMapper.deleteByPrimaryKey(uid);
    }

    // TODO 这里需要有一个定时任务
    @Override
    @Transactional
    public int insertArticle(Article record) {
        Assert.notNull(record, "插入的文章数据不能为null");
        record.setUid(GenerateInfoUtils.generateUid(auroraProperties.getSnowFlakeWorkerId(),
                auroraProperties.getSnowFlakeDatacenterId()));
        // 创建时间是自动生成的
        record.setDelete(false);
        record.setCommentUids(null);
        setUserUid(record);
        // 查询可用的类别和分类
        setCategory(record);
        setTag(record);
        setTimingPublishTime(record);
        return articleMapper.insertSelective(record);
    }

    @Override
    public PageData<ArticleVO> selectByCondition(Condition<Long> condition) {
        Assert.notNull(condition, "查询条件不能为null");
        return PageUtils.pageList(condition, t -> articleMapper.selectByCondition(condition), ArticleVO.class);
    }

    @Override
    public ArticleVO selectByUid(Long uid) {
        return BeanUtils.getSingleObjFromList(articleMapper.selectByCondition(Condition.instant(uid, true)), ArticleVO.class);
    }

    @Transactional
    @Override
    public int updateByPrimaryKeySelective(Article record) {
        Assert.notNull(record, "文章数据不能为null");
        setCategory(record);
        setTag(record);
        record.setUserUid(null);
        setTimingPublishTime(record);
        return articleMapper.updateByPrimaryKeySelective(record);
    }

    private void setCategory(Article article) {
        // 如果文章中的某个标签或者类别不存在，则添加
        if (!StringUtils.hasLength(article.getCategoryNames())) {
            return;
        }

        String categoryNames = Arrays.stream(article.getCategoryNames().split(","))
                .distinct()
                .filter(categoryName -> {
                    // 如果不存在，则添加
                    Category category = Category.builder().title(categoryName).build();
                    if (categoryService.selectByTitle(categoryName) == null) {
                        // 不存在，添加
                        return categoryService.insertSelective(category) == 1;
                    }
                    return true;
                })
                .collect(Collectors.joining(","));
        article.setCategoryNames(categoryNames);

    }

    private void setTag(Article article) {
        if (!StringUtils.hasLength(article.getTagNames())) {
            return;
        }

        String tagNames = Arrays.stream(article.getTagNames().split(","))
                .distinct()
                .filter(tagName -> {
                    // 如果不存在，则添加
                    Tag tag = Tag.builder().title(tagName).build();
                    if (tagService.selectByTitle(tagName) == null) {
                        // 不存在，添加
                        return tagService.insertSelective(tag) == 1;
                    }
                    return true;
                })
                .collect(Collectors.joining(","));
        article.setTagNames(tagNames);
    }

    private void setUserUid(Article article) {
        JwtUserInfo jwtUserInfo = UserUtils.getCurrentUser();
        AssertUtils.stateThrow(jwtUserInfo != null,
                () -> new UserException(ResponseStatusCodeEnum.PERMISSION_USER_NOT_LOGIN));
        article.setUserUid(jwtUserInfo.getUserUid());
    }

    /**
     * 判断公告对象中的定时发布时间是否规范，如果不规范，则设置为null
     * @param article
     */
    private void setTimingPublishTime(Article article) {
        if (article.getTiming() == null || !article.getTiming()) {
            article.setTimingPublishTime(null);
            return;
        }
        if (!TimeUtils.isTimingPublishTime(article.getTimingPublishTime())) {
            article.setTiming(false);
            article.setTimingPublishTime(null);
            return;
        }
        article.setTimingPublishTime(DateUtils.parse(article.getTimingPublishTime()));
    }
}
