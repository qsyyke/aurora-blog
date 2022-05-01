package xyz.xcye.amqp.service.impl;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import xyz.xcye.aurora.feign.MessageLogFeignService;
import xyz.xcye.aurora.properties.AuroraProperties;
import xyz.xcye.amqp.service.SendMQMessageService;
import xyz.xcye.common.dto.StorageSendMailInfo;
import xyz.xcye.common.po.table.CommentDO;
import xyz.xcye.common.po.table.MessageLogDO;
import xyz.xcye.core.enums.RegexEnum;
import xyz.xcye.core.enums.ResponseStatusCodeEnum;
import xyz.xcye.amqp.enums.SendHtmlMailTypeNameEnum;
import xyz.xcye.core.exception.AuroraException;
import xyz.xcye.core.exception.email.EmailException;
import xyz.xcye.core.util.ConvertObjectUtils;
import xyz.xcye.core.util.ValidationUtils;
import xyz.xcye.core.util.id.GenerateInfoUtils;
import xyz.xcye.core.valid.Insert;

import javax.annotation.Resource;
import javax.validation.groups.Default;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author qsyyke
 * @date Created in 2022/4/28 08:41
 */

@Slf4j
@Service
public class SendMQMessageServiceImpl implements SendMQMessageService {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private AuroraProperties auroraProperties;

    @Resource
    private MessageLogFeignService messageLogFeignService;

    @Override
    public void sendReplyMail(CommentDO replyingCommentInfo, CommentDO repliedCommentInfo, String exchangeName,
                              String exchangeType, String routingKey) throws BindException {
        // 组装新评论对象，发送给被评论页面所对应的用户
        StorageSendMailInfo mailInfo = new StorageSendMailInfo();
        mailInfo.setSubject(replyingCommentInfo.getContent());
        mailInfo.setUserUid(repliedCommentInfo.getUserUid());
        setCorrelationDataId(mailInfo);

        // 如果不是回复评论的话，则直接传入userUid便可以，会通过此userUid查询对应的email，但是如果是回复评论，则需要在此处进行设置收件人邮箱
        // 优先级：receiverEmail > 通过userUid查询到的email
        mailInfo.setReceiverEmail(repliedCommentInfo.getEmail());
        mailInfo.setSendType(SendHtmlMailTypeNameEnum.REPLY_COMMENT);

        List<Map<String,Object>> list = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put(SendHtmlMailTypeNameEnum.RECEIVE_COMMENT.getKeyName(),replyingCommentInfo);
        map.put(SendHtmlMailTypeNameEnum.REPLY_COMMENT.getKeyName(), repliedCommentInfo);
        list.add(map);

        // 将组装的map集合转换成json字符串，发送到交换机
        mailInfo = ConvertObjectUtils.generateMailInfo(mailInfo, list);

        // 组装一个存放被回复评论对象的数据
        Map<String,Object> repliedMap = new HashMap<>();
        repliedMap.put(SendHtmlMailTypeNameEnum.ADDITIONAL_DATA.getKeyName(), replyingCommentInfo);
        mailInfo.setAdditionalData(repliedMap);
        String msgJson = ConvertObjectUtils.jsonToString(mailInfo);

        sendMQMsg(mailInfo, msgJson, exchangeName, routingKey, exchangeType);
    }

    @Override
    public void sendCommonMail(StorageSendMailInfo sendMailInfo, String exchangeName, String exchangeType,
                               String routingKey, List<Map<String,Object>> replacedObjList)
            throws AuroraException, BindException {
        if (isLegitimateReceiverEmail(sendMailInfo)) {
            throw new EmailException(ResponseStatusCodeEnum.EXCEPTION_EMAIL_EXISTS);
        }
        setCorrelationDataId(sendMailInfo);

        // 将发送的回复评论数据组装成一个map集合
        String msgJson = ConvertObjectUtils.generateMailJson(sendMailInfo, replacedObjList);
        sendMQMsg(sendMailInfo, msgJson, exchangeName, routingKey, exchangeType);
    }

    @Override
    public void sendSimpleTextMail(StorageSendMailInfo sendMailInfo, String exchangeName, String exchangeType,
                                   String routingKey) throws AuroraException, BindException {
        // 发送简单文本
        if (!StringUtils.hasLength(sendMailInfo.getSimpleText())) {
            // 没有简单文本，不做处理
            return;
        }

        if (isLegitimateReceiverEmail(sendMailInfo)) {
            throw new EmailException(ResponseStatusCodeEnum.EXCEPTION_EMAIL_EXISTS);
        }

        setCorrelationDataId(sendMailInfo);

        //将发送的回复评论数据组装成一个map集合
        String msgJson = ConvertObjectUtils.generateMailJson(sendMailInfo, null);
        sendMQMsg(sendMailInfo, msgJson, exchangeName, routingKey, exchangeType);
    }

    /**
     * 判断待发送的邮件对象中的数据，是否合法
     * @param mailInfo
     * @return
     */
    private boolean isLegitimateReceiverEmail(StorageSendMailInfo mailInfo) {
        if (!StringUtils.hasLength(mailInfo.getReceiverEmail()) && mailInfo.getUserUid() == null) {
            return true;
        }

        if (StringUtils.hasLength(mailInfo.getReceiverEmail())) {
            boolean matches = Pattern.matches(RegexEnum.MAIL_REGEX.getRegex(), mailInfo.getReceiverEmail());
            if (!matches) {
                mailInfo.setReceiverEmail(null);
            }
        }
        return false;
    }

    private void setCorrelationDataId(StorageSendMailInfo mailInfo) {
        if (!StringUtils.hasLength(mailInfo.getCorrelationDataId())) {
            mailInfo.setCorrelationDataId(GenerateInfoUtils.generateUid(auroraProperties.getSnowFlakeWorkerId(), auroraProperties.getSnowFlakeDatacenterId()) + "");
        }
    }

    private MessageLogDO setMessageLogDO(String message,long uid,String exchange,
                                         String queue,String routingKey,boolean ackStatus,
                                         int tryCount,String exchangeType,boolean consumeStatus,
                                         String errorMessage) {
        MessageLogDO messageLogDO = new MessageLogDO();
        messageLogDO.setMessage(message);
        messageLogDO.setUid(uid);
        messageLogDO.setExchange(exchange);
        messageLogDO.setRoutingKey(routingKey);
        messageLogDO.setAckStatus(ackStatus);
        messageLogDO.setTryCount(tryCount);
        messageLogDO.setExchangeType(exchangeType);
        messageLogDO.setConsumeStatus(consumeStatus);
        messageLogDO.setErrorMessage(errorMessage);
        return messageLogDO;
    }

    private void sendMQMsg(StorageSendMailInfo sendMailInfo, String msgJson, String exchangeName, String routingKey, String exchangeType) throws BindException {
        CorrelationData correlationData = new CorrelationData(sendMailInfo.getCorrelationDataId());
        // 调用feign向数据库中插入mq消息
        insertMessageLogData(sendMailInfo, msgJson, exchangeName, routingKey, exchangeType);
        rabbitTemplate.send(exchangeName, routingKey, new Message(msgJson.getBytes(StandardCharsets.UTF_8)), correlationData);
    }

    private void insertMessageLogData(StorageSendMailInfo mailInfo, String msgJson, String exchangeName, String routingKey, String exchangeType) throws BindException {
        //向au_message_log表中插入生产信息
        MessageLogDO messageLogDO = setMessageLogDO(msgJson, Long.parseLong(mailInfo.getCorrelationDataId()), exchangeName, "",
                routingKey, false, 0, exchangeType, false, "");
        // 验证messageLogDO对象属性是否合法
        ValidationUtils.valid(messageLogDO, Insert.class, Default.class);
        messageLogFeignService.insertMessageLog(messageLogDO);
    }

    /**
     * 如果从交换机中发送到某个队列的消息不符合规范，则将此消息发送到错误交换机中进行消费
     * @param msg
     * @param channel
     * @param message
     * @throws IOException
     */
    @Override
    public void sendMistakeMessageToExchange(String msg, Channel channel, Message message) throws IOException {
        // 任何一个出问题，都表示生产者发送的消息不合法，将此消息发送到mistakeMessageExchange交换机 因为这个消息是没有用的，所以也就不更新数据库了
        rabbitTemplate.send(RabbitMQNameConstant.MISTAKE_MESSAGE_EXCHANGE, RabbitMQNameConstant.MISTAKE_MESSAGE_ROUTING_KEY,new Message(msg.getBytes(StandardCharsets.UTF_8)));
        //在此处需要应答
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
