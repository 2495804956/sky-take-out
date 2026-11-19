package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import io.swagger.annotations.ApiOperation;

import java.util.List;

public interface ShoppingCartService {

    /**
     * 添加购物车
     */
    void add(ShoppingCartDTO shoppingCartDTO);

    /**
     * 购物车列表
     */
    List<ShoppingCart> list();

    /**
     * 清空购物车
     */
    void clean();
}
