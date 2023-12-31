package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
    /**
     * 新增菜品和对应的口味
     * @param dishDTO
     */
    public void saveWithFlavor(DishDTO dishDTO);

    /**
     * 分页查询菜品
     * @param dishPageQueryDTO
     * @return
     */
    PageResult pageQuary(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 批量删除菜品
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 根据id查询菜品和口味数据
     * @param id
     * @return
     */
    DishVO getByIdWirhFlavor(Long id);

    /**
     * 更新菜品和口味数据
     * @param dishDTO
     */
    void updateWithFlavor(DishDTO dishDTO);

    List<Dish> list(Long categoryId);

    List<DishVO> listWithFlavor(Dish dish);

    void forbiddenOrEnable(Integer status, Long id);
}
