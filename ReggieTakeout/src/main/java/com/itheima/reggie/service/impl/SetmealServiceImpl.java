package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;

    @Override
    public void saveWithDish(SetmealDto setmealDto) {
        //添加套餐信息
        this.save(setmealDto);

        List<SetmealDish> dishes = setmealDto.getSetmealDishes();
        //给每个dish添加套餐的id
        dishes = dishes.stream().map(item -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        //将所有菜品添加到菜品表中
        setmealDishService.saveBatch(dishes);
    }

    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
        //判断是否存在未停售的套餐
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper();
        lqw.in(Setmeal::getId,ids);
        lqw.eq(Setmeal::getStatus,1);
        int count = this.count(lqw);
        if(count > 0){
            throw new CustomException("不允许删除未停售的套餐");
        }

        //删除套餐
        this.removeByIds(ids);

        //删除套餐对应的菜品
        LambdaQueryWrapper<SetmealDish> lqw1 = new LambdaQueryWrapper<>();
        lqw1.in(SetmealDish::getSetmealId,ids);
        setmealDishService.remove(lqw1);
    }


}
