package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ShopService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api(tags = {"商铺管理接口"})
@Slf4j
public class ShopController {

    public static final String SHOP_STATUS = "SHOP_STATUS";
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ShopService shopService;
    /**
     * 设置商铺状态
     * @param status
     */
    @PutMapping("/{status}")
    @ApiOperation("设置商铺状态")
    public Result setStatus(@PathVariable Integer status){
        redisTemplate.opsForValue().set(SHOP_STATUS,status);
        return Result.success();
    }

    /**
     * 获取商铺状态
     * @return
     */
    @GetMapping("/status")
    @ApiOperation("获取商铺状态")
    public Result<Integer> getStatus(){
        Integer status = (Integer) redisTemplate.opsForValue().get(SHOP_STATUS);
        return Result.success(status);
    }
}
