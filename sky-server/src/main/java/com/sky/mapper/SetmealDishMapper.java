package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    /**
     * 根据菜品ID列表获取套餐ID列表
     */
    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);

    /**
     * 批量插入套餐菜品
     */
    void insertBatch(List<SetmealDish> setmealDishes);

    /**
     * 根据套餐ID列表删除套餐菜品
     */
    void deleteBySetmealIds(List<Long> setmealIds);

    /**
     * 根据套餐ID查询套餐菜品
     */
    @Select("SELECT * FROM setmeal_dish WHERE setmeal_id = #{setmealId}")
    List<SetmealDish> getBySetmealId(Long setmealId);
}
