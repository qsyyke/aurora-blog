package xyz.xcye.admin.manager.mq;

import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import xyz.xcye.common.constant.RabbitMQNameConstant;

@Component
public class AdminRabbitMQExchangeRegister {

    /**
     * 注册一个普通发送邮件的交换机
     * @return
     */
    @Bean
    public Exchange mailReplyCommentNoticeExchange() {
        return new TopicExchange(RabbitMQNameConstant.AURORA_SEND_EMAIL_COMMON_EXCHANGE,
                true,true);
    }

    /**
     * 注册一个死信发送邮件的交换机
     * @return
     */
    @Bean
    public Exchange mailReceiveCommentNoticeExchange() {
        return new TopicExchange(RabbitMQNameConstant.AURORA_SEND_EMAIL_DEAD_LETTER_EXCHANGE,
                true, false);
    }

}