package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.WorkSpaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin/workspace")
@Slf4j
@Api(tags = "工作台接口")
public class WorkSpaceController {
    @Autowired
    private WorkSpaceService workSpaceService;

    /**
     * 今日营业数据
     * @return
     */
    @GetMapping("/businessData")
    @ApiOperation("营业数据")
    public Result<BusinessDataVO> businessData(){
        LocalDateTime begin = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime end = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        BusinessDataVO businessDataVO = workSpaceService.getBusinessData(begin, end);
        return Result.success(businessDataVO);
    }

    /**
     * 查询订单概览
     * @return
     */
    @GetMapping("/overviewOrders")
    @ApiOperation("订单概览")
    public Result<OrderOverViewVO> orderOverView(){
        OrderOverViewVO orderOverViewVO = workSpaceService.getOrderOverView();
        return Result.success(orderOverViewVO);
    }

    /**
     * 查询菜品概览
     * @return
     */
    @GetMapping("/overviewDishes")
    @ApiOperation("菜品概览")
    public Result<DishOverViewVO> dishOverView(){
        DishOverViewVO dishOverViewVO = workSpaceService.getDishOverView();
        return Result.success(dishOverViewVO);
    }

    /**
     * 查询套餐概览
     * @return
     */
    @GetMapping("/overviewSetmeals")
    @ApiOperation("查询套餐概览")
    public Result<SetmealOverViewVO> setmealOverView(){
        SetmealOverViewVO setmealOverViewVO = workSpaceService.getSetmealOverView();
        return Result.success(setmealOverViewVO);
    }
}
