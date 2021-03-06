package xyz.xcye.comment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.validation.BindException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import xyz.xcye.comment.dto.CommentDTO;
import xyz.xcye.comment.po.Comment;
import xyz.xcye.comment.service.CommentService;
import xyz.xcye.comment.vo.CommentVO;
import xyz.xcye.comment.vo.ShowCommentVO;
import xyz.xcye.core.annotaion.controller.ModifyOperation;
import xyz.xcye.core.annotaion.controller.SelectOperation;
import xyz.xcye.core.util.NetWorkUtils;
import xyz.xcye.core.valid.Insert;
import xyz.xcye.core.valid.Update;
import xyz.xcye.data.entity.Condition;
import xyz.xcye.data.entity.PageData;

import javax.servlet.http.HttpServletRequest;
import javax.validation.groups.Default;

/**
 * @author qsyyke
 */

@Tag(name = "评论相关操作接口")
@RequestMapping("/comment")
@RestController
@RefreshScope
public class CommentController {
    @Autowired
    private CommentService commentService;

    @Operation(summary = "更新评论")
    @ModifyOperation
    @PutMapping("")
    public int updateComment(@Validated({Update.class}) Comment comment) {
        return commentService.updateComment(comment);
    }

    @Operation(summary = "插入新评论")
    @ModifyOperation
    @PostMapping("")
    public int insertComment(@Validated({Default.class, Insert.class}) Comment comment,
                             HttpServletRequest request) throws Throwable {
        comment.setCommentIp(NetWorkUtils.getIpAddr(request));
        comment.setOperationSystemInfo(NetWorkUtils.getOperationInfo(request));
        return commentService.insertComment(comment);
    }

    @Operation(summary = "删除单条评论")
    @ModifyOperation
    @DeleteMapping("/{uid}")
    public int deleteComment(@PathVariable("uid") Long uid) {
        return commentService.deleteComment(uid);
    }

    /**
     * 根据多个uid，返回这些uid所对应的记录以及他们的子评论数据 是所有，没有做分页操作，其中uid是在文章中获取的
     * @param commentUidArr
     * @return
     */
    @Operation(summary = "查询所有满足要求的所有评论")
    @SelectOperation
    @GetMapping("/queryArticleComments")
    public ShowCommentVO queryAllComment(@RequestParam(value = "uidArr") long[] commentUidArr) {
        return commentService.queryArticleComments(commentUidArr);
    }

    @Operation(summary = "根据自定义条件查询所有评论")
    @SelectOperation
    @GetMapping
    public PageData<CommentVO> queryAllCommentByCondition(Condition<Long> condition) {
        return commentService.queryAllComments(condition);
    }

    @Operation(summary = "根据uid查询评论")
    @SelectOperation
    @GetMapping("/{uid}")
    public CommentDTO queryCommentByUid(@PathVariable("uid") long uid) {
        return commentService.queryByUid(uid);
    }

    @Operation(summary = "重新发送评论的邮件通知")
    @ModifyOperation
    @PostMapping("/resendEmail/{uid}")
    public int resendEmailNotice(@PathVariable("uid") long uid) throws BindException {
        return commentService.resendEmailNotice(uid);
    }
}
