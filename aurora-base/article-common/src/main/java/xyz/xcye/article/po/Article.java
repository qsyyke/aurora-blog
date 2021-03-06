package xyz.xcye.article.po;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import xyz.xcye.core.constant.FieldLengthConstant;
import xyz.xcye.core.valid.Delete;
import xyz.xcye.core.valid.Insert;
import xyz.xcye.core.valid.Update;
import xyz.xcye.core.valid.validator.ValidateString;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Schema(title = "文章")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Article implements Serializable {
    /**
    * 唯一uid
    */
    @Schema(title = "唯一uid")
    @NotNull(groups = {Update.class, Delete.class})
    private Long uid;

    /**
    * 文章是否展示评论，0：否，1：是
    */
    @Schema(title = "文章是否展示评论，0：否，1：是")
    private Boolean showComment;

    /**
    * 文章有附件的话，附件的uid集合
    */
    @Schema(title = "文章有附件的话，附件的uid集合")
    private String accessoryUids;

    /**
    * 文章类别uid集合
    */
    @Schema(title = "文章类别uid集合")
    private String categoryNames;

    /**
    * 文章标签uid集合
    */
    @Schema(title = "文章标签uid集合")
    private String tagNames;

    /**
    * 文章是否发布，1：发布，0：不发布
    */
    @Schema(title = "文章是否发布，1：发布，0：不发布")
    private Boolean publish;

    /**
    * 发布此篇文章的用户uid
    */
    @Schema(title = "发布此篇文章的用户uid")
    private Long userUid;

    /**
    * 是否原创，1：原创 0：不是原创
    */
    @Schema(title = "是否原创，1：原创 0：不是原创")
    private Boolean originalArticle;

    /**
    * 如果是原创，则原创链接
    */
    @Schema(title = "如果是原创，则原创链接")
    @Length(max = FieldLengthConstant.URL)
    private String originalArticleUrl;

    /**
    * 文章封面对应的图片uid
    */
    @Schema(title = "文章封面对应的图片uid")
    @Length(max = FieldLengthConstant.URL)
    private String coverPictureUrl;

    /**
    * 文章标题
    */
    @Schema(title = "文章标题")
    @Length(max = FieldLengthConstant.TITLE)
    @ValidateString(value = "文章标题", max = FieldLengthConstant.TITLE, groups = {Insert.class})
    private String title;

    /**
    * 文章简介
    */
    @Length(max = FieldLengthConstant.SUMMARY)
    @Schema(title = "文章简介")
    private String summary;

    /**
    * 是否定时发布 0： 不定时，1：定时
    */
    @Schema(title = "是否定时发布 0： 不定时，1：定时")
    private Boolean timing;

    /**
    * 定时发布时间
    */
    @Schema(title = "定时发布时间")
    private String timingPublishTime;

    /**
    * 文章点赞数
    */
    @Schema(title = "文章点赞数")
    private Integer likeNumber;

    /**
    * 阅读量
    */
    @Schema(title = "阅读量")
    private Integer readNumber;

    /**
    * 此篇文章最后修改的时间
    */
    @Schema(title = "此篇文章最后修改的时间")
    private String updateTime;

    /**
    * 文章发布时间
    */
    @Schema(title = "文章发布时间")
    private String createTime;

    /**
    * 是否删除 逻辑删除 1： 已删除
    */
    @Schema(title = "是否删除 逻辑删除 1： 已删除")
    private Boolean delete;

    /**
    * 文章内容
    */
    @Schema(title = "文章内容")
    @ValidateString(value = "文章内容", max = FieldLengthConstant.CONTENT, groups = Insert.class)
    @Length(max = FieldLengthConstant.CONTENT)
    private String content;

    /**
    * 此篇文章对应的评论uid集合，只需要设置所有父评论的uid
    */
    @Schema(title = "此篇文章对应的评论uid集合，只需要设置所有父评论的uid")
    private String commentUids;

    private static final long serialVersionUID = 1L;
}