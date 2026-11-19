package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j

public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        // 判断当前用户是否已经添加过该菜品或套餐
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.selectList(shoppingCart);

        if (shoppingCartList != null && shoppingCartList.size() > 0) {
            // 如果已经添加过，则更新数量
            ShoppingCart existShoppingCart = shoppingCartList.get(0);
            existShoppingCart.setNumber(existShoppingCart.getNumber() + 1);
            shoppingCartMapper.updateById(existShoppingCart);
        } else {
            // 如果没有添加过，则新增
            // 判断本次添加的是菜品还是套餐
            Long dishId = shoppingCartDTO.getDishId();
            if (dishId != null) {
                // 本次添加的是菜品
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setAmount(dish.getPrice());
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
            } else {
                // 本次添加的是套餐
                Long setmealId = shoppingCartDTO.getSetmealId();
                Setmeal setmeal = setmealMapper.getById(setmealId);
                shoppingCart.setAmount(setmeal.getPrice());
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());

            shoppingCartMapper.insert(shoppingCart);
        }

    }

    /**
     * 购物车列表
     */
    @Override
    public List<ShoppingCart> list() {
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.selectList(shoppingCart);
        return shoppingCartList;

    }

    /**
     * 清空购物车
     */
    @Override
    public void clean() {
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.clean(userId);

    }

    /**
     * 删除购物车中一个商品
     */
    @Override
    public void delete(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        // 判断购物车该商品的数量是否大于1，如果大于1则减1，否则删除该商品
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.selectList(shoppingCart);
        if (shoppingCartList != null && shoppingCartList.size() > 0) {
            ShoppingCart existShoppingCart = shoppingCartList.get(0);
            if (existShoppingCart.getNumber() > 1) {
                existShoppingCart.setNumber(existShoppingCart.getNumber() - 1);
                shoppingCartMapper.updateById(existShoppingCart);
            } else {
                shoppingCartMapper.deleteByCondition(shoppingCart);
            }
        }

    }
}
