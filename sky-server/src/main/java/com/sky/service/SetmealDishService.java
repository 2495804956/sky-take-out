package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealDishService {
    /**
     * 新增套餐菜品
     */
    public void saveWithDish(SetmealDTO setmealDTO);

    /**
     * 套餐分页查询
     */
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);


    /**
     * 批量删除套餐
     */
    void deleteBatch(List<Long> ids);

    /**
     * 根据id查询套餐和菜品
     */
    SetmealVO getByIdWithDishes(Long id);

    /**
     * 更新套餐和菜品
     */
    void updateWithDish(SetmealDTO setmealDTO);

    /**
     * 套餐起售停售
     */
    void startOrStop(Integer status, Long id);
}
