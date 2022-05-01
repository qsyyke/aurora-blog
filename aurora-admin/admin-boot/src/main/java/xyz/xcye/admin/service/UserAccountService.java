package xyz.xcye.admin.service;


import xyz.xcye.core.dto.Condition;
import xyz.xcye.core.entity.result.ModifyResult;
import xyz.xcye.common.po.UserAccountDO;
import xyz.xcye.common.vo.UserAccountVO;

import java.util.List;

/**
 * 用户权限的servie层
 * @author qsyyke
 */

public interface UserAccountService {
    /**
     * 插入一条账户信息
     * @param userAccountDO
     * @return
     */
    ModifyResult insert(UserAccountDO userAccountDO);

    /**
     * 更新账户信息
     * @param userAccountDO
     * @return
     */
    ModifyResult update(UserAccountDO userAccountDO);

    /**
     * 根据uid删除一条账户信息
     * @param uid
     * @return
     */
    ModifyResult deleteByUid(long uid);

    /**
     * 根据条件查询所有数据
     * @param condition 其中otherUid为userUid，keyword为role
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    List<UserAccountVO> queryAllByCondition(Condition<Long> condition) throws ReflectiveOperationException;

    /**
     * 通过uid查询
     * @param uid
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    UserAccountVO queryByUid(long uid) throws ReflectiveOperationException;

    /**
     * 通过userUid查询
     * @param userUid
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    UserAccountVO queryByUserUid(long userUid) throws ReflectiveOperationException;
}
