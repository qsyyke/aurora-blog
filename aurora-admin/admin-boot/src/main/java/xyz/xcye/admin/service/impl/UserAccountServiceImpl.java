package xyz.xcye.admin.service.impl;

import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.xcye.admin.dao.UserAccountDao;
import xyz.xcye.admin.service.UserAccountService;
import xyz.xcye.aurora.properties.AuroraProperties;
import xyz.xcye.core.dto.Condition;
import xyz.xcye.core.entity.result.ModifyResult;
import xyz.xcye.common.po.UserAccountDO;
import xyz.xcye.core.enums.ResponseStatusCodeEnum;
import xyz.xcye.core.util.BeanUtils;
import xyz.xcye.core.util.DateUtils;
import xyz.xcye.core.util.id.GenerateInfoUtils;
import xyz.xcye.common.vo.UserAccountVO;

import java.util.Date;
import java.util.List;

/**
 * @author qsyyke
 */

@Service
public class UserAccountServiceImpl implements UserAccountService {

    @Autowired
    private AuroraProperties auroraProperties;

    @Autowired
    private UserAccountDao userAccountDao;

    @Override
    public ModifyResult insert(UserAccountDO userAccountDO) {
        userAccountDO = UserAccountDO.builder()
                .uid(GenerateInfoUtils.generateUid(auroraProperties.getSnowFlakeWorkerId(),auroraProperties.getSnowFlakeDatacenterId()))
                .createTime(DateUtils.format(new Date())).accountLocked(false)
                .accountExpired(false).role(userAccountDO.getRole())
                .permission(userAccountDO.getPermission())
                .delete(false)
                .userUid(userAccountDO.getUserUid()).build();
        return ModifyResult.operateResult(userAccountDao.insert(userAccountDO),"插入账户信息",
                ResponseStatusCodeEnum.SUCCESS.getCode(),userAccountDO.getUid());
    }

    @Override
    public ModifyResult update(UserAccountDO userAccountDO) {
        userAccountDO.setUpdateTime(DateUtils.format(new Date()));
        return ModifyResult.operateResult(userAccountDao.update(userAccountDO),"修改账户信息",
                ResponseStatusCodeEnum.SUCCESS.getCode(), userAccountDO.getUid());
    }

    @Override
    public ModifyResult deleteByUid(long uid) {
        return ModifyResult.operateResult(userAccountDao.deleteByUid(uid),"删除账户信息",
                ResponseStatusCodeEnum.SUCCESS.getCode(), uid);
    }

    @Override
    public List<UserAccountVO> queryAllByCondition(Condition<Long> condition) throws ReflectiveOperationException {
        condition.init(condition);
        PageHelper.startPage(condition.getPageNum(), condition.getPageSize(), condition.getOrderBy());
        return BeanUtils.copyList(userAccountDao.queryAllByCondition(condition),UserAccountVO.class);
    }

    @Override
    public UserAccountVO queryByUid(long uid) throws ReflectiveOperationException {
        Condition<Long> condition = Condition.instant(uid, Long.class, true);
        return BeanUtils.getSingleObjFromList(userAccountDao.queryAllByCondition(condition),UserAccountVO.class);
    }

    @Override
    public UserAccountVO queryByUserUid(long userUid) throws ReflectiveOperationException {
        Condition<Long> condition = Condition.instant(userUid, Long.class, false);
        return BeanUtils.getSingleObjFromList(userAccountDao.queryAllByCondition(condition), UserAccountVO.class);
    }
}
