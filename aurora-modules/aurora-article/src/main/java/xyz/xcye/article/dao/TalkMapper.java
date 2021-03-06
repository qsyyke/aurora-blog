package xyz.xcye.article.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xyz.xcye.article.po.Talk;
import xyz.xcye.data.entity.Condition;

import java.util.List;

@Mapper
public interface TalkMapper {
    /**
     * delete by primary key
     * @param uid primaryKey
     * @return deleteCount
     */
    int deleteByPrimaryKey(Long uid);

    /**
     * insertArticle record to table selective
     * @param record the record
     * @return insertArticle count
     */
    int insertSelective(Talk record);

    /**
     * select by primary key
     * @param condition 查询条件 其中keyword->content(模糊查询),status->is_show_comment,show->is_show
     * @return object by primary key
     */
    List<Talk> selectByCondition(@Param("condition") Condition<Long> condition);

    /**
     * update record selective
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKeySelective(Talk record);
}