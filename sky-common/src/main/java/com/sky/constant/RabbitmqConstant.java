package com.sky.constant;

public class RabbitmqConstant {
    public static final String MARK_ORDER_PAY_QUEUE = "mark.order.pay.queue";
    public static final String ORDER_PAY_EXCHANGE_TOPIC = "pay.topic";
    public static final String ORDER_PAY_ROUTING_KEY = "pay.success";
    public static final String CART_CLEAR_QUEUE = "cart.clear.queue";
    public static final String CART_EXCHANGE_TOPIC = "cart.topic";
    public static final String CART_CLEAR_ROUTING_KEY = "order.create";
    public static final String DELAY_EXCHANGE = "trade.delay.topic";
    public static final String DELAY_ORDER_QUEUE = "trade.order.delay.queue";
    public static final String DELAY_ORDER_ROUTING_KEY = "order.query";
    public static final String CREATE_ORDER_EXCHANGE_TOPIC = "create.order.topic";
    public static final String ORDER_QUEUE = "order.queue";
    public static final String CREATE_ORDER_ROUTING_KEY = "create.order";
}
