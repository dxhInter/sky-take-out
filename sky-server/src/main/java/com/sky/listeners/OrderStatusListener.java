package com.sky.listeners;

import com.sky.constant.MessageConstant;
import com.sky.constant.RabbitmqConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrderCreateDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.message.MultiDelayMessage;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author dxh
 * @version 1.0
 * @project sky-take-out
 * @date 2024/2/16 13:36:46
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderStatusListener {
    private final RabbitTemplate rabbitTemplate;
    private final OrderService orderService;
    private final OrderMapper orderMapper;
    private final ShoppingCartMapper shoppingCartMapper;
    private final OrderDetailMapper orderDetailMapper;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = RabbitmqConstant.DELAY_ORDER_QUEUE, durable = "true"),
            exchange = @Exchange(name = RabbitmqConstant.DELAY_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = RabbitmqConstant.DELAY_ORDER_ROUTING_KEY
    ))
    public void listenOrderPayDelayMessage(MultiDelayMessage<Long> msg){
        Long orderId = msg.getData();
        Orders orders = orderMapper.getById(orderId);
        if (orders == null || orders.getPayStatus().equals(Orders.CANCELLED)) {
            // 订单不存在或者已经处理，直接返回
            return;
        }
        if (orders.getPayStatus().equals(Orders.PAID)) {
            // 订单已经支付，直接返回
            String outTradeNo = orders.getNumber();
            orderService.paySuccess(outTradeNo);
            return;
        }
        // 未支付，获取下次检查时间，重新发送延迟消息
        if (msg.hasNextDelay()){
            int delayVal = msg.removeNextDelay().intValue();
            rabbitTemplate.convertAndSend(RabbitmqConstant.DELAY_EXCHANGE, RabbitmqConstant.DELAY_ORDER_ROUTING_KEY, msg, message -> {
                message.getMessageProperties().setDelay(delayVal);
                return message;
            });
            return;
        }
        // 标记订单状态为已取消
        try {
            orderService.cancelByUnpaid(orderId);
        } catch (Exception e) {
            log.error("订单取消失败", e);
        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = RabbitmqConstant.ORDER_QUEUE, durable = "true"),
            exchange = @Exchange(name = RabbitmqConstant.CREATE_ORDER_EXCHANGE_TOPIC, type = ExchangeTypes.TOPIC),
            key = RabbitmqConstant.CREATE_ORDER_ROUTING_KEY
    ))
    public void listenCreateOrder(OrderCreateDTO orderCreateDTO){

        Long userId = orderCreateDTO.getUserId();
        //insert an order into order table
        Orders orders = orderCreateDTO.getOrders();
        AddressBook addressBook = orderCreateDTO.getAddressBook();
        ShoppingCart shoppingCart = orderCreateDTO.getShoppingCart();
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if (shoppingCartList == null || shoppingCartList.size() == 0){
            //throw exception
            throw new AddressBookBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(orders.UN_PAID);
        orders.setStatus(orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(userId);
        orders.setAddress(addressBook.getProvinceName()+addressBook.getCityName()+addressBook.getDistrictName()+addressBook.getDetail());
        orderMapper.insert(orders);

        List<OrderDetail> orderDetailList = new ArrayList<>();
        //insert order detail into order detail table
        for (ShoppingCart cart:shoppingCartList){
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart,orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }

        orderDetailMapper.insertBatch(orderDetailList);

    }
}
