package xyz.xcye.comment.service;

import xyz.xcye.core.dto.Condition;
import xyz.xcye.comment.dto.CommentDTO;
import xyz.xcye.core.entity.result.ModifyResult;
import xyz.xcye.comment.po.CommentDO;
import xyz.xcye.comment.vo.CommentVO;

import java.util.List;

/**
 * 评论的service层
 * @author qsyyke
 */

public interface CommentService {
    /**
     * 插入一条评论数据，根据commentDTO对象
     * @return
     */
    ModifyResult insertComment(CommentDO commentDO)
            throws Throwable;

    /**
     * 根据uid删除对应的记录，是真正的从数据库中删除此条记录
     * @param uid
     * @return
     */
    ModifyResult deleteComment(Long uid);

    /**
     * 修改一条评论数据，根据commentDTO对象
     * @param commentDO
     * @return
     */
    ModifyResult updateComment(CommentDO commentDO);

    ModifyResult updateDeleteStatus(CommentDO commentDO);

    /**
     * 根据传入的arrayCommentUid评论uid数据，获取对应的所有评论节点数据
     * @param arrayCommentUid
     * @return
     */
    CommentVO queryArticleComments(long[] arrayCommentUid) throws ReflectiveOperationException;

    /**
     * 根据传入的arrayCommentUid评论uid数据，获取对应的所有评论节点数据
     * @param condition
     * @return
     */
    List<CommentDTO> queryAllComments(Condition<Long> condition);

    CommentDTO queryByUid(long uid) throws ReflectiveOperationException;
}
