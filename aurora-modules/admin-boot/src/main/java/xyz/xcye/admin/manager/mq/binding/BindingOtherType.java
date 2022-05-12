package xyz.xcye.admin.manager.mq.binding;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import xyz.xcye.core.constant.amqp.AmqpExchangeNameConstant;
import xyz.xcye.core.constant.amqp.AmqpQueueNameConstant;

/**
 * 将没有做区别的队列和相应的交换机绑定起来
 * @author qsyyke
 */

@Component
public class BindingOtherType {
    /**
     * 将mistakeMessageExchange交换机和队列绑定起来
     * @return
     */
    @Bean
    public Binding mistakeMessageBinding() {
        return BindingBuilder.bind(new Queue(AmqpQueueNameConstant.MISTAKE_MESSAGE_QUEUE))
                .to(new DirectExchange(AmqpExchangeNameConstant.MISTAKE_MESSAGE_EXCHANGE))
                .with(AmqpQueueNameConstant.MISTAKE_MESSAGE_ROUTING_KEY);
    }
}
