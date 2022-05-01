package xyz.xcye.message.service.impl;

import com.github.pagehelper.PageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindException;
import xyz.xcye.aurora.properties.AuroraProperties;
import xyz.xcye.core.dto.Condition;
import xyz.xcye.core.entity.result.ModifyResult;
import xyz.xcye.core.entity.result.R;
import xyz.xcye.message.po.EmailDO;
import xyz.xcye.core.enums.ResponseStatusCodeEnum;
import xyz.xcye.core.exception.AuroraException;
import xyz.xcye.core.exception.email.EmailException;
import xyz.xcye.core.exception.user.UserException;
import xyz.xcye.core.util.BeanUtils;
import xyz.xcye.core.util.ConvertObjectUtils;
import xyz.xcye.core.util.DateUtils;
import xyz.xcye.core.util.JSONUtils;
import xyz.xcye.core.util.id.GenerateInfoUtils;
import xyz.xcye.message.vo.EmailVO;
import xyz.xcye.common.vo.UserVO;
import xyz.xcye.message.dao.EmailDao;
import xyz.xcye.message.feign.UserFeignService;
import xyz.xcye.message.service.EmailService;

import java.util.Date;
import java.util.List;

/**
 * @author qsyyke
 */

@Service
public class EmailServiceImpl implements EmailService {

    private final static Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private EmailDao emailDao;

    @Autowired
    private AuroraProperties auroraProperties;

    @Autowired
    private UserFeignService userFeignService;

    @Override
    public ModifyResult insertEmail(EmailDO email)
            throws BindException, ReflectiveOperationException, AuroraException {
        // 判断邮箱是否已经存在
        if (queryByEmail(email.getEmail()) != null) {
            throw new EmailException(ResponseStatusCodeEnum.EXCEPTION_EMAIL_EXISTS);
        }

        R userR = userFeignService.queryUserByUid(email.getUserUid());
        UserVO userVO = JSONUtils.parseObjFromResult(ConvertObjectUtils.jsonToString(userR), "data", UserVO.class);

        if (userVO == null || userVO.getUid() == null) {
            throw new UserException(ResponseStatusCodeEnum.PERMISSION_USER_NOT_EXIST);
        }

        // 判断是否绑定 如果用户没有验证邮箱，也重新绑定
        if (userVO.getVerifyEmail()) {
            throw new EmailException(ResponseStatusCodeEnum.EXCEPTION_EMAIL_HAD_BINDING);
        }

        //生成一个uid
        long uid = GenerateInfoUtils.generateUid(auroraProperties.getSnowFlakeWorkerId(), auroraProperties.getSnowFlakeDatacenterId());
        //其中user_uid应该在调用的此方法的时候，就已经赋值在email对象里面
        email.setUid(uid);
        email.setCreateTime(DateUtils.format(new Date()));
        int insertEmailNum = emailDao.insertEmail(email);
        return ModifyResult.operateResult(insertEmailNum,"插入email数据",
                 ResponseStatusCodeEnum.SUCCESS.getCode(),email.getUid());
    }

    @Override
    public ModifyResult deleteEmailByUid(long uid) {
        //验证uid是否有效
        if (uid == 0) {
            return ModifyResult.operateResult(ResponseStatusCodeEnum.PARAM_IS_INVALID.getMessage(),
                    0,ResponseStatusCodeEnum.PARAM_IS_INVALID.getCode(),uid);
        }
        //删除
        int deleteEmailNum = emailDao.deleteEmailByUid(uid);
        return ModifyResult.operateResult(deleteEmailNum,"删除" + uid + "对应的email数据",
                ResponseStatusCodeEnum.SUCCESS.getCode(),uid);
    }

    @Override
    public ModifyResult updateEmail(EmailDO email) {
        //判断uid是否有效
        if (email == null) {
            return ModifyResult.operateResult(ResponseStatusCodeEnum.PARAM_IS_INVALID.getMessage(),
                    0,ResponseStatusCodeEnum.PARAM_IS_INVALID.getCode(), email.getUid());
        }
        email.setUpdateTime(DateUtils.format(new Date()));
        int updateEmailByUidNum = emailDao.updateEmail(email);
        return ModifyResult.operateResult(updateEmailByUidNum,"修改" + email.getUid() + "对应的email数据",
                ResponseStatusCodeEnum.SUCCESS.getCode(), email.getUid());
    }

    @Override
    public List<EmailVO> queryAllEmail(Condition<Long> condition) throws ReflectiveOperationException {
        condition = condition.init(condition);
        PageHelper.startPage(condition.getPageNum(),condition.getPageSize(),condition.getOrderBy());
        return BeanUtils.copyList(emailDao.queryAllEmail(condition), EmailVO.class);
    }

    @Override
    public EmailVO queryByUid(long uid) throws ReflectiveOperationException {
        Condition<Long> condition = new Condition<>();
        condition.setUid(uid);
        return BeanUtils.getSingleObjFromList(emailDao.queryAllEmail(condition),EmailVO.class);
    }

    @Override
    public EmailVO queryByUserUid(long userUid) throws ReflectiveOperationException {
        Condition<Long> condition = new Condition<>();
        condition.setOtherUid(userUid);
        return BeanUtils.getSingleObjFromList(emailDao.queryAllEmail(condition), EmailVO.class);
    }

    @Override
    public EmailVO queryByEmail(String email) throws ReflectiveOperationException {
        Condition<Long> condition = new Condition();
        condition.setKeyword(email);
        return BeanUtils.getSingleObjFromList(emailDao.queryAllEmail(condition), EmailVO.class);
    }
}
