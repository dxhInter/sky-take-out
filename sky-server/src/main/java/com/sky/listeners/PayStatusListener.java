package com.sky.listeners;

import com.sky.constant.RabbitmqConstant;
import com.sky.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 使用基于RabbitMQ监听支付状态
 */

@Component
@RequiredArgsConstructor
public class PayStatusListener {
    private final OrderService orderService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = RabbitmqConstant.MARK_ORDER_PAY_QUEUE, durable = "true"),
            exchange = @Exchange(value = RabbitmqConstant.ORDER_PAY_EXCHANGE_TOPIC, type = ExchangeTypes.TOPIC),
            key = RabbitmqConstant.ORDER_PAY_ROUTING_KEY
    ))
    public void listenPaySuccess(String outTradeNo){
        orderService.paySuccess(outTradeNo);
    }
}
