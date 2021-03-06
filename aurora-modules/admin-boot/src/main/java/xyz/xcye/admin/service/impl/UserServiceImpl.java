package xyz.xcye.admin.service.impl;

import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import xyz.xcye.admin.api.feign.EmailFeignService;
import xyz.xcye.admin.dao.UserMapper;
import xyz.xcye.admin.dto.EmailVerifyAccountDTO;
import xyz.xcye.admin.enums.GenderEnum;
import xyz.xcye.admin.po.User;
import xyz.xcye.admin.properties.AdminDefaultProperties;
import xyz.xcye.admin.service.UserService;
import xyz.xcye.admin.vo.UserVO;
import xyz.xcye.api.mail.sendmail.entity.StorageSendMailInfo;
import xyz.xcye.api.mail.sendmail.enums.SendHtmlMailTypeNameEnum;
import xyz.xcye.api.mail.sendmail.service.SendMQMessageService;
import xyz.xcye.api.mail.sendmail.util.AccountInfoUtils;
import xyz.xcye.api.mail.sendmail.util.StorageEmailVerifyUrlUtil;
import xyz.xcye.aurora.properties.AuroraProperties;
import xyz.xcye.auth.constant.AuthRedisConstant;
import xyz.xcye.core.constant.amqp.AmqpExchangeNameConstant;
import xyz.xcye.core.constant.amqp.AmqpQueueNameConstant;
import xyz.xcye.core.entity.R;
import xyz.xcye.core.enums.ResponseStatusCodeEnum;
import xyz.xcye.core.exception.email.EmailException;
import xyz.xcye.core.exception.user.UserException;
import xyz.xcye.core.util.BeanUtils;
import xyz.xcye.core.util.ConvertObjectUtils;
import xyz.xcye.core.util.JSONUtils;
import xyz.xcye.core.util.id.GenerateInfoUtils;
import xyz.xcye.core.util.lambda.AssertUtils;
import xyz.xcye.data.entity.Condition;
import xyz.xcye.data.entity.PageData;
import xyz.xcye.data.util.PageUtils;
import xyz.xcye.message.vo.EmailVO;

import java.util.*;

/**
 * @author qsyyke
 */

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final String bindEmailKey = "bindEmail";

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailFeignService emailFeignService;
    @Autowired
    private AuroraProperties auroraProperties;
    @Autowired
    private AuroraProperties.AuroraAccountProperties auroraAccountProperties;
    @Autowired
    private AdminDefaultProperties adminDefaultProperties;
    @Autowired
    private SendMQMessageService sendMQMessageService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int insertUserSelective(User user)
            throws UserException {
        // ???????????????????????????
        AssertUtils.stateThrow(!existsUsername(user.getUsername()),
                () -> new UserException(ResponseStatusCodeEnum.PERMISSION_USER_EXIST));
        // ??????????????????
        setUserProperties(user);
       return userMapper.insertSelective(user);
    }

    @Transactional
    @Override
    public int updateUserSelective(User user) throws UserException {
        Objects.requireNonNull(user, "?????????????????????null");
        // ????????????????????????
        Optional.ofNullable(user.getPassword()).ifPresent(t -> user.setPassword(null));

        if (StringUtils.hasLength(user.getUsername()) && existsUsername(user.getUsername())) {
            //throw new UserException(ResponseStatusCodeEnum.PERMISSION_USER_EXIST);
            // ????????????????????????????????????
            user.setUsername(null);
        }
        // ????????????????????????????????????userUid???????????????????????????
        String username = getUsername(user.getUid());
        int updateNum = userMapper.updateByPrimaryKeySelective(user);
        if (updateNum == 1) {
            removeUserDetailsFromRedis(username);
        }
        return updateNum;
    }

    /**
     * ?????????????????????????????????????????????????????????????????????????????????username,originPwd,newPwd?????????????????????????????????????????????username???secretKey
     * @param username
     * @param originPwd
     * @param newPwd
     * @return
     */
    @Override
    public int updatePassword(String username, String originPwd, String newPwd) {
        AssertUtils.stateThrow(StringUtils.hasLength(username), () -> new UserException("?????????????????????"));
        // ??????????????????????????????
        User user = queryByUsernameContainPassword(username);
        AssertUtils.stateThrow(user != null, () -> new UserException(ResponseStatusCodeEnum.PERMISSION_USER_NOT_EXIST));
        // ????????????????????????
        boolean matches = passwordEncoder.matches(user.getPassword(), originPwd);
        AssertUtils.stateThrow(matches, () -> new UserException("????????????"));

        // ????????????
        user.setPassword(passwordEncoder.encode(newPwd));
        int updateNum = userMapper.updateByPrimaryKeySelective(user);
        if (updateNum == 1) {
            removeUserDetailsFromRedis(user.getUsername());
        }
        return updateNum;
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????????????????????????????????????????email???????????????email????????????????????????????????????????????????????????????
     * ???html??????email???
     * @param username
     * @return
     */
    @Override
    public int forgotPassword(String username) {

        return 0;
    }

    @Transactional
    @Override
    public int realDeleteByUid(long uid) {
        return userMapper.deleteByPrimaryKey(uid);
    }

    @Override
    public int logicDeleteByUid(long uid) {
        User user = User.builder().delete(true).uid(uid).build();
        return updateUserSelective(user);
    }

    @Override
    public PageData<UserVO> queryAllByCondition(Condition<Long> condition) {
        return PageUtils.pageList(condition, t -> userMapper.selectByCondition(condition), UserVO.class);
    }

    @Override
    public UserVO queryUserByUid(long uid) {
        return BeanUtils.getSingleObjFromList(userMapper.selectByCondition(Condition.instant(uid, true)), UserVO.class);
    }

    @Override
    public User queryByUsernameContainPassword(String username) {
        return BeanUtils.getSingleObjFromList(userMapper.selectByCondition(Condition.instant(username, null, null)), User.class);
    }

    @Override
    public User queryByUidContainPassword(long uid) {
        return BeanUtils.getSingleObjFromList(userMapper.selectByCondition(Condition.instant(uid, true, null, null)), User.class);
    }

    @Override
    public UserVO queryUserByUsername(String username) {
        return BeanUtils.getSingleObjFromList(userMapper.selectByCondition(Condition.instant(username, null, null)), UserVO.class);
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Override
    public int bindingEmail(String email) throws BindException, EmailException {
        AssertUtils.stateThrow(StringUtils.hasLength(email), () -> new EmailException(ResponseStatusCodeEnum.PARAM_IS_INVALID));
        // ????????????aurora-message??????????????????email???uid????????????
        R r = emailFeignService.queryByEmail(email);
        EmailVO queriedEmailInfo = JSONUtils.parseObjFromResult(ConvertObjectUtils.jsonToString(r), "data", EmailVO.class);

        AssertUtils.ifNullThrow(queriedEmailInfo,
                () -> new EmailException(ResponseStatusCodeEnum.EXCEPTION_EMAIL_NOT_EXISTS));
        AssertUtils.ifNullThrow(queriedEmailInfo.getEmail(),
                () -> new EmailException(ResponseStatusCodeEnum.EXCEPTION_EMAIL_NOT_EXISTS));
        User user = User.builder()
                .emailUid(queriedEmailInfo.getUid())
                .uid(queriedEmailInfo.getUserUid())
                .build();
        // ???????????????????????????
        UserVO userVO = queryUserByUid(user.getUid());
        AssertUtils.stateThrow(!userVO.getVerifyEmail(), () -> new EmailException(ResponseStatusCodeEnum.EXCEPTION_EMAIL_HAD_BINDING));
        // ??????????????????????????????
        AssertUtils.stateThrow(!userVO.getDelete(), () -> new UserException(ResponseStatusCodeEnum.PERMISSION_USER_NOT_DELETE));
        if (userVO.getVerifyEmail() != null && userVO.getVerifyEmail()) {
            throw new EmailException(ResponseStatusCodeEnum.EXCEPTION_EMAIL_HAD_BINDING);
        }

        // ????????????????????????????????????????????????????????????????????????????????????????????????emailUid
        int updateUserNum = updateUserSelective(user);
        if (updateUserNum == 1) {
            sendVerifyEmail(userVO, queriedEmailInfo);
        }

        // ??????redis????????????
        return updateUserNum;
    }

    /**
     * ??????????????????????????????????????????
     * @param username
     * @return
     */
    private boolean existsUsername(String username) {
        Condition<Long> condition = Condition.instant(username, null, null);
        return !userMapper.selectByCondition(condition).isEmpty();
    }

    private String getUsername(Long userUid) {
        UserVO userVO = queryUserByUid(userUid);
        return userVO == null ? "" : userVO.getUsername();
    }

    private void setUserProperties(User user) {
        user.setDelete(false);
        user.setVerifyEmail(false);
        user.setAccountLock(false);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setUid(GenerateInfoUtils.generateUid(auroraProperties.getSnowFlakeWorkerId(),auroraProperties.getSnowFlakeDatacenterId()));

        if (!StringUtils.hasLength(user.getNickname())) {
            user.setNickname(adminDefaultProperties.getNickname());
        }

        if (!StringUtils.hasLength(user.getAvatar())) {
            user.setAvatar(adminDefaultProperties.getAvatar());
        }

        // ????????????????????????????????????????????????(0)
        user.setGender(Optional.ofNullable(user.getGender()).orElse(GenderEnum.SECRET));
    }

    private void sendVerifyEmail(UserVO userVO, EmailVO emailVO) throws BindException {
        String verifyAccountUrl = AccountInfoUtils.generateVerifyUrl(userVO.getUid(),
                bindEmailKey, userVO.hashCode(), auroraAccountProperties.getMailVerifyAccountPrefixPath());
        EmailVerifyAccountDTO verifyAccountInfo = EmailVerifyAccountDTO.builder()
                .userUid(userVO.getUid())
                .expirationTime(auroraAccountProperties.getMailVerifyAccountExpirationTime())
                .verifyAccountUrl(verifyAccountUrl)
                .receiverEmail(emailVO.getEmail()).subject(null).build();

        List<Map<SendHtmlMailTypeNameEnum, Object>> list = new ArrayList<>();
        Map<SendHtmlMailTypeNameEnum, Object> map = new HashMap<>();
        map.put(SendHtmlMailTypeNameEnum.VERIFY_ACCOUNT, verifyAccountInfo);
        list.add(map);

        StorageSendMailInfo mailInfo = new StorageSendMailInfo();
        mailInfo.setReceiverEmail(emailVO.getEmail());
        mailInfo.setSendType(SendHtmlMailTypeNameEnum.VERIFY_ACCOUNT);
        mailInfo.setSubject(userVO.getUsername() + " ???????????????????????????");
        mailInfo.setUserUid(userVO.getUid());

        // ??????????????????????????????redis????????????
        boolean storageVerifyUrlToRedis = StorageEmailVerifyUrlUtil.storageVerifyUrlToRedis(bindEmailKey, userVO.hashCode(),
                auroraAccountProperties.getMailVerifyAccountExpirationTime(), userVO.getUid());
        if (!storageVerifyUrlToRedis) {
            throw new UserException("??????????????????");
        }
        sendMQMessageService.sendCommonMail(mailInfo, AmqpExchangeNameConstant.AURORA_SEND_MAIL_EXCHANGE,
                "topic", AmqpQueueNameConstant.SEND_HTML_MAIL_ROUTING_KEY, list);
    }

    private void removeUserDetailsFromRedis(String username) {
        // ????????????????????????redis??????userService??????
        redisTemplate.delete(AuthRedisConstant.USER_DETAILS_CACHE_PREFIX + username);
    }
}
