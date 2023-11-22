package com.sky.service;

import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;

import java.time.LocalDateTime;

public interface WorkSpaceService {
    /**
     * 今日营业数据
     * @param begin
     * @param end
     * @return
     */
    BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end);

    /**
     * 查询订单概览
     * @return
     */
    OrderOverViewVO getOrderOverView();

    /**
     * 查询菜品概览
     * @return
     */
    DishOverViewVO getDishOverView();

    /**
     * 查询套餐概览
     * @return
     */
    SetmealOverViewVO getSetmealOverView();
}
