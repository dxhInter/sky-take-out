package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.RabbitmqConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.*;
import com.sky.message.MultiDelayMessage;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import io.jsonwebtoken.lang.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WebSocketServer webSocketServer;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    /**
     * 提交订单
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //handld exception to of the business logic suah as address or cart is empty
//        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
//        if(addressBook == null){
//            //throw exception
//            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
//        }
//
//        ShoppingCart shoppingCart = new ShoppingCart();
//        Long userId = BaseContext.getCurrentId();
//        shoppingCart.setUserId(userId);
//        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
//        if (shoppingCartList == null || shoppingCartList.size() == 0){
//            //throw exception
//            throw new AddressBookBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
//        }
//
//        //insert an order into order table
//        Orders orders = new Orders();
//        BeanUtils.copyProperties(ordersSubmitDTO,orders);
//        orders.setOrderTime(LocalDateTime.now());
//        orders.setPayStatus(orders.UN_PAID);
//        orders.setStatus(orders.PENDING_PAYMENT);
//        orders.setNumber(String.valueOf(System.currentTimeMillis()));
//        orders.setPhone(addressBook.getPhone());
//        orders.setConsignee(addressBook.getConsignee());
//        orders.setUserId(userId);
//        orders.setAddress(addressBook.getProvinceName()+addressBook.getCityName()+addressBook.getDistrictName()+addressBook.getDetail());
//
//        orderMapper.insert(orders);
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if(addressBook == null){
            //throw exception
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
//        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
//        if (shoppingCartList == null || shoppingCartList.size() == 0){
//            //throw exception
//            throw new AddressBookBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
//        }
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,orders);
        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();
        orderCreateDTO.setAddressBook(addressBook);
        orderCreateDTO.setUserId(userId);
        orderCreateDTO.setShoppingCart(shoppingCart);
        orderCreateDTO.setOrders(orders);
        try {
            rabbitTemplate.convertAndSend(RabbitmqConstant.CREATE_ORDER_EXCHANGE_TOPIC, RabbitmqConstant.CREATE_ORDER_ROUTING_KEY, orderCreateDTO);
        }catch (Exception e){
            log.error("创建订单失败",e);
        }

//        List<OrderDetail> orderDetailList = new ArrayList<>();
//        //insert order detail into order detail table
//        for (ShoppingCart cart:shoppingCartList){
//            OrderDetail orderDetail = new OrderDetail();
//            BeanUtils.copyProperties(cart,orderDetail);
//            orderDetail.setOrderId(orders.getId());
//            orderDetailList.add(orderDetail);
//        }
//
//        orderDetailMapper.insertBatch(orderDetailList);

        //delete shopping cart
//        shoppingCartMapper.deleteByUserId(userId);
        // 基于RabbitMQ的异步通知实现清空购物车
        try {
            rabbitTemplate.convertAndSend(RabbitmqConstant.CART_EXCHANGE_TOPIC, RabbitmqConstant.CART_CLEAR_ROUTING_KEY, userId);
        }catch (Exception e){
            log.error("清空用户{}购物车失败",userId,e);
        }
        try {
            // 发送延迟消息，用于检查订单是否支付
            MultiDelayMessage<Long> msg = MultiDelayMessage.of(orders.getId(),
                    10000L, 10000L, 10000L, 10000L, 10000L, 10000L, 20000L, 20000L, 20000L,
                    60000L, 60000L, 60000L,600000L);
            rabbitTemplate.convertAndSend(RabbitmqConstant.DELAY_EXCHANGE, RabbitmqConstant.DELAY_ORDER_ROUTING_KEY, msg);
        } catch (Exception e) {
            log.error("发送延迟消息失败", e);
        }
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();
        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @Override
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) {
        // 当前登录用户id
         Long userId = BaseContext.getCurrentId();
         User user = userMapper.getById(userId);

        // 直接调用paySuccess方法，模拟支付成功
//        paySuccess(ordersPaymentDTO.getOrderNumber());
        String orderNumber = ordersPaymentDTO.getOrderNumber();
        try {
            rabbitTemplate.convertAndSend(RabbitmqConstant.ORDER_PAY_EXCHANGE_TOPIC, RabbitmqConstant.ORDER_PAY_ROUTING_KEY, orderNumber);
        }catch (Exception e){
            log.error("订单支付失败",e);
        }
        // 调用微信支付接口，生成预支付交易单
        // JSONObject jsonObject = weChatPayUtil.pay(
        // ordersPaymentDTO.getOrderNumber(), // 商户订单号
        // new BigDecimal(0.01), // 支付金额，单位 元
        // "苍穹外卖订单", // 商品描述
        // user.getOpenid() // 微信用户的openid
        // );

         JSONObject jsonObject = new JSONObject();
         if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
         }
         OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
         vo.setPackageStr(jsonObject.getString("package"));
         return vo;
    }

    /**
     * 支付成功, 保证消息处理的幂等性
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);
        // 业务状态判断, 保证消息处理的幂等性
        if(ordersDB == null || !ordersDB.getStatus().equals(Orders.PENDING_PAYMENT)){
            return;
        }
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);

        //通过websocket推送消息
        Map map = new HashMap<>();
        map.put("type", 1);//1，表示来单提醒 2表示客户催单
        map.put("orderNumber", ordersDB.getId());
        map.put("content", "订单号"+outTradeNo);

        String json =JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }

    /**
     * 历史订单查询
     * @param pageNum
     * @param pageSize
     * @param status
     * @return
     */
    @Override
    public PageResult pageQuery4User(int pageNum, int pageSize, Integer status) {
        PageHelper.startPage(pageNum,pageSize);

        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        ordersPageQueryDTO.setStatus(status);

        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        List<OrderVO> list = new ArrayList<>();
        if(page!=null&&page.size()>0){
            for(Orders orders:page){
                Long orderId = orders.getId();
                List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orderId);
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders,orderVO);
                orderVO.setOrderDetailList(orderDetailList);
                list.add(orderVO);
            }
        }
        return new PageResult(page.getTotal(),list);
    }

    /**
     * 订单详情
     * @param id
     * @return
     */
    @Override
    public OrderVO details(Long id) {
        Orders orders = orderMapper.getById(id);
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());

        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders,orderVO);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    /**
     * 用户取消订单
     * @param id
     * @throws Exception
     */
    @Override
    public void userCancelById(Long id)throws Exception {
        Orders orderDB = orderMapper.getById(id);
        if(orderDB==null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        if(orderDB.getStatus() > 2){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(orderDB.getId());

        if(orderDB.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            orders.setStatus(Orders.CANCELLED);
        }
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消订单");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 再来一单
     * @param id
     */
    @Override
    public void repetition(Long id) {
        Long userId = BaseContext.getCurrentId();
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);
        List<ShoppingCart> shoppingCartList = orderDetails.stream().map(orderDetail -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail,shoppingCart, "id");
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());

            return shoppingCart;
        }).collect(Collectors.toList());
        shoppingCartMapper.insertBatch(shoppingCartList);
    }

    /**
     * 分页条件查询订单
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);
        List<OrderVO> orderVOList = getOrderVOList(page);
        return new PageResult(page.getTotal(),orderVOList);
    }



    public List<OrderVO> getOrderVOList(Page<Orders> page){
        List<OrderVO> orderVOList = new ArrayList<>();

        List<Orders> ordersList = page.getResult();
        if(!Collections.isEmpty(ordersList)){
            for(Orders orders:ordersList){
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders,orderVO);
                String orderDishes = getOrderDishes(orders);

                // 将订单菜品信息封装到orderVO中，并添加到orderVOList
                orderVO.setOrderDishes(orderDishes);
                orderVOList.add(orderVO);
            }
        }
        return orderVOList;
    }

    public String getOrderDishes(Orders orders){
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());
        // 将每一条订单菜品信息拼接为字符串（格式：宫保鸡丁*3;）
        List<String> orderDishesList = orderDetailList.stream().map(x -> {
            String orderDishes = x.getName() + "*" + x.getNumber()+";";
            return orderDishes;
        }).collect(Collectors.toList());
        return String.join("",orderDishesList);
    }

    /**
     * 订单统计
     * @return
     */
    @Override
    public OrderStatisticsVO statistics() {
        Integer toBeConfirmed = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.countStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;
    }

    /**
     * 确认订单
     * @param ordersConfirmDTO
     */
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();

        orderMapper.update(orders);
    }

    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception{
        Orders ordersDB = orderMapper.getById(ordersRejectionDTO.getId());
        if(ordersDB == null||!ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Integer status = ordersDB.getPayStatus();
        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        if(status==Orders.PAID){
            //退款
            log.info("退款");
            orders.setStatus(Orders.CANCELLED);
        }


        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    @Override
    public void delivery(Long id) {
        Orders ordersDB = orderMapper.getById(id);
        if(ordersDB == null || !ordersDB.getStatus().equals(Orders.CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.update(orders);
    }

    @Override
    public void complete(Long id) {
        Orders ordersDB = orderMapper.getById(id);
        if(ordersDB == null || !ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        Orders ordersDB = orderMapper.getById(ordersCancelDTO.getId());

        Integer status = ordersDB.getPayStatus();
        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        if(status==Orders.PAID){
            //退款
            log.info("退款");
            orders.setStatus(Orders.CANCELLED);
        }
        orders.setId(ordersCancelDTO.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 催单
     * @param id
     */
    @Override
    public void reminder(Long id) {
        //check if the order is exist
        Orders ordersDB = orderMapper.getById(id);
        if(ordersDB == null){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Map map = new HashMap<>();
        map.put("type", 2);//1，表示来单提醒 2表示客户催单
        map.put("orderId", id);
        map.put("content", "订单号"+ordersDB.getNumber());
        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }

    /**
     * 超时未支付订单取消
     * @param id 订单id
     */
    @Override
    public void cancelByUnpaid(Long id) throws Exception {
        Orders ordersDB = orderMapper.getById(id);
        // 业务状态判断
        if(ordersDB == null || !ordersDB.getStatus().equals(Orders.PENDING_PAYMENT)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("超时未支付取消订单");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }
}
