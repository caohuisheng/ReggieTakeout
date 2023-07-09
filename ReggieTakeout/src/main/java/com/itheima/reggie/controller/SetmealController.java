package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("套餐信息：{}",setmealDto);

        setmealService.saveWithDish(setmealDto);

        return R.success("保存套餐信息成功");
    }

    /**
     * 分页查询所有套餐
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        log.info("page = {},pagesize = {}, name = {}",page,pageSize,name);
        Page<Setmeal> pageInfo = new Page<>(page,pageSize);
        Page<SetmealDto> pageDto = new Page<>();

        //设置查询条件
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.like(name!=null,Setmeal::getName,name);
        lqw.orderByDesc(Setmeal::getUpdateTime);

        //执行分页查询
        setmealService.page(pageInfo, lqw);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo,pageDto,"records");
        List<Setmeal> records = pageInfo.getRecords();

        //将setmeal转换为setmealDto
        List<SetmealDto> list = records.stream().map(item -> {
            SetmealDto setmealDto = new SetmealDto();
            //拷贝基本数据
            BeanUtils.copyProperties(item,setmealDto);
            //得到分类名
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if(category != null){
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());
        pageDto.setRecords(list);

        return R.success(pageDto);
    }

    /**
     * 根据id删除套餐数据
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> deleteByIds(@RequestParam List<Long> ids){
        log.info("ids = {}",ids);
        setmealService.removeWithDish(ids);
        return R.success("删除成功");
    }

    /**
     * 根据分类id查询所有
     */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        //构造查询条件
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Setmeal::getCategoryId, setmeal.getCategoryId());
        lqw.eq(Setmeal::getStatus,setmeal.getStatus());

        List<Setmeal> list = setmealService.list(lqw);

        return R.success(list);
    }


}
