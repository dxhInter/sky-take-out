package com.sky.controller.user;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.google.common.hash.BloomFilter;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.service.SetmealService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 菜品管理
 */
@RestController("userDishController")
@RequestMapping("/user/dish")
@Api(tags = "菜品相关接口")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private BloomFilter<Long> categoryBloomFilter;

    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId){
        if (!categoryBloomFilter.mightContain(categoryId)) {
            log.error("布隆过滤器说这个键可能不存在，直接返回空集合");
            return Result.success();
        }

        String key="dish_"+categoryId;

        String dishJSON = stringRedisTemplate.opsForValue().get(key);
        List<DishVO> list = null;
        if (StrUtil.isNotBlank(dishJSON)) {
            list = JSONUtil.toList(JSONUtil.parseArray(dishJSON), DishVO.class);
            return Result.success(list);
        }
//        List<DishVO> list = (List<DishVO>) redisTemplate.opsForValue().get(key);
//        if (list != null&& list.size()>0) {
//            return Result.success(list);
//        }

        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);
        list = dishService.listWithFlavor(dish);

//        redisTemplate.opsForValue().set(key,list);
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(list));
        return Result.success(list);
    }

}
