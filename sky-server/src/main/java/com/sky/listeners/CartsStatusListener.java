package com.sky.listeners;

import com.sky.constant.RabbitmqConstant;
import com.sky.mapper.ShoppingCartMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 使用基于RabbitMQ监听购物车状态
 */

@Component
@RequiredArgsConstructor
public class CartsStatusListener {
    private final ShoppingCartMapper shoppingCartMapper;


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = RabbitmqConstant.CART_CLEAR_QUEUE, durable = "true"),
            exchange = @Exchange(value = RabbitmqConstant.CART_EXCHANGE_TOPIC, type = ExchangeTypes.TOPIC),
            key = RabbitmqConstant.CART_CLEAR_ROUTING_KEY
    ))
    public void listenCartsStatus(Long userId){
        shoppingCartMapper.deleteByUserId(userId);
    }
}
