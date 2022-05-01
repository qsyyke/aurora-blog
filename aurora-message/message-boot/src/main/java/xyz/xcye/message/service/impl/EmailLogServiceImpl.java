package xyz.xcye.message.service.impl;

import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindException;
import xyz.xcye.message.po.EmailLogDO;
import xyz.xcye.core.dto.Condition;
import xyz.xcye.core.entity.result.ModifyResult;
import xyz.xcye.core.enums.ResponseStatusCodeEnum;
import xyz.xcye.core.util.BeanUtils;
import xyz.xcye.core.util.DateUtils;
import xyz.xcye.core.util.ValidationUtils;
import xyz.xcye.core.valid.Insert;
import xyz.xcye.message.vo.EmailLogVO;
import xyz.xcye.message.dao.EmailLogDao;
import xyz.xcye.message.service.EmailLogService;

import javax.validation.groups.Default;
import java.util.Date;
import java.util.List;

/**
 * @author qsyyke
 */

@Service
public class EmailLogServiceImpl implements EmailLogService {

    @Autowired
    private EmailLogDao emailLogDao;

    @Override
    public ModifyResult insertEmailLog(EmailLogDO emailLog) {
        //因为au_email_log表中的uid是自增的，可以不用设置uid
        //设置创建时间
        emailLog.setCreateTime(DateUtils.format(new Date()));
        int insertEmailLogNum = emailLogDao.insertEmailLog(emailLog);
        return ModifyResult.operateResult(
                insertEmailLogNum,"插入邮件发送日志",
                ResponseStatusCodeEnum.SUCCESS.getCode(), emailLog.getUid());
    }

    @Override
    public ModifyResult updateEmailLog(EmailLogDO emailLog) throws BindException {
        //参数验证
        ValidationUtils.valid(emailLog, Insert.class, Default.class);
        int updateEmailLogNum = emailLogDao.updateEmailLog(emailLog);
        return ModifyResult.operateResult(updateEmailLogNum, "修改邮件发送日志",
                ResponseStatusCodeEnum.SUCCESS.getCode(), emailLog.getUid());
    }

    @Override
    public ModifyResult deleteEmailLog(long uid) {
        int deleteEmailLogNum = emailLogDao.deleteEmailLog(uid);
        return ModifyResult.operateResult(deleteEmailLogNum,"删除" + uid + "对应的邮件发送日志",
                ResponseStatusCodeEnum.SUCCESS.getCode(), uid);
    }

    @Override
    public List<EmailLogVO> queryAll(Condition<Long> condition) throws ReflectiveOperationException {
        condition = condition.init(condition);
        PageHelper.startPage(condition.getPageNum(),condition.getPageSize(),condition.getOrderBy());
        return BeanUtils.copyList(emailLogDao.queryAll(condition),EmailLogVO.class);
    }

    @Override
    public EmailLogVO queryByUid(long uid) throws ReflectiveOperationException {
        Condition<Long> condition = new Condition<>();
        condition.setUid(uid);
        return BeanUtils.getSingleObjFromList(emailLogDao.queryAll(condition), EmailLogVO.class);
    }
}
