package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequestMapping("/shoppingCart")
@RestController
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加商品到购物车
     * @param shoppingCart
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        log.info("shoppingCart:",shoppingCart);
        //得到用户的id
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId,userId);

        //判断添加的是菜品还是套餐
        Long dishId = shoppingCart.getDishId();
        if(dishId != null){
            lqw.eq(ShoppingCart::getDishId,dishId);
        }else{
            lqw.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }

        //判断购物车中是否存在添加的菜品或套餐
        ShoppingCart shoppingCart1 = shoppingCartService.getOne(lqw);
        if(shoppingCart1 != null){
            Integer number = shoppingCart1.getNumber();
            shoppingCart1.setNumber(number+1);
            shoppingCartService.updateById(shoppingCart1);
        }else{
            shoppingCart.setNumber(1);
            shoppingCartService.save(shoppingCart);
            shoppingCart1 = shoppingCart;
        }

        return R.success(shoppingCart1);
    }

    /**
     * 根据用户id查询购物车数据
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        log.info("查询购物车数据...");
        //得到用户id
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();

        lqw.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> list = shoppingCartService.list(lqw);

        return R.success(list);
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean(){
        //得到用户id
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId,userId);
        shoppingCartService.remove(lqw);

        return R.success("清空购物车成功");
    }
}
