package xyz.xcye.message.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xyz.xcye.message.po.MessageLogDO;
import xyz.xcye.core.dto.Condition;

import java.util.List;

/**
 * @author qsyyke
 */

@Mapper
public interface MessageLogDao {
    int insertMessageLog(@Param("messageDO") MessageLogDO messageLogDO);
    int deleteMessageLog(@Param("uid") long uid);
    int updateMessageLog(@Param("messageDO") MessageLogDO messageLogDO);

    /**
     *
     * @param condition 查询条件，其中keyword对应routingKey,status对应consume_status
     * @return
     */
    List<MessageLogDO> queryAllMessageLog(@Param("condition") Condition condition);

}
