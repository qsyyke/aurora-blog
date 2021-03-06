package xyz.xcye.comment.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xyz.xcye.comment.po.Comment;
import xyz.xcye.data.entity.Condition;

import java.util.List;

@Mapper
public interface CommentMapper {
    /**
     * delete by primary key
     * @param uid primaryKey
     * @return deleteCount
     */
    int deleteByPrimaryKey(Long uid);

    /**
     * insert record to table selective
     * @param record the record
     * @return insert count
     */
    int insertSelective(Comment record);

    /**
     * select by primary key
     * @param uid primary key
     * @return object by primary key
     */
    List<Comment> selectByCondition(@Param("condition") Condition condition);

    /**
     * update record selective
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKeySelective(Comment record);
}