package xyz.xcye.admin.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xyz.xcye.core.dto.Condition;
import xyz.xcye.common.po.UserDO;

import java.util.List;

@Mapper
public interface UserDao {
    int insertUser(@Param("userDO") UserDO userDO);

    int updateUser(@Param("userDO") UserDO userDO);

    int deleteByUid(@Param("uid") long uid);

    /**
     * 根据条件查询用户信息
     * @param condition 查询条件，其中keyword为username，status为verifAccount,otherUid为user_account_uid
     * @return
     */
    List<UserDO> queryAllByCondition(@Param("condition") Condition condition);
}