package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.AddressBook;
import com.itheima.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/addressBook")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 添加地址
     * @param addressBook
     * @return
     */
    @PostMapping
    public R<String> saveAddress(@RequestBody AddressBook addressBook){
        log.info("address{}",addressBook);
        Long userId = BaseContext.getCurrentId();
        addressBook.setUserId(userId);
        addressBookService.save(addressBook);
        return R.success("保存地址成功");
    }

    /**
     * 根据用户id查询所有地址
     * @return
     */
    @GetMapping("/list")
    public R<List<AddressBook>> list(){
        Long userId = BaseContext.getCurrentId();
        log.info("userId=={}",userId);
        //Select * from AddressBook where userId = ?
        //构造查询条件
        LambdaQueryWrapper<AddressBook> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AddressBook::getUserId,userId);
        List<AddressBook> list = addressBookService.list(lqw);
        return R.success(list);
    }

    /**
     * 设置默认地址
     * @param addressBook
     * @return
     */
    @PutMapping("/default")
    public R<String> setDefaultAddress(@RequestBody AddressBook addressBook){
        Long userId = BaseContext.getCurrentId();
        //将用户所有的地址都设置为非默认的 Updata AddressBook set isDefault = 0 where userId = ?
        LambdaUpdateWrapper<AddressBook> lqw = new LambdaUpdateWrapper<>();
        lqw.eq(AddressBook::getUserId,userId);
        lqw.set(AddressBook::getIsDefault,0);
        addressBookService.update(lqw);

        //设置选中的地址为默认地址
        addressBook.setIsDefault(1);
        addressBookService.updateById(addressBook);

        return R.success("设置默认地址成功");
    }

    /**
     * 获取默认用户地址
     */
    @GetMapping("/default")
    public R<AddressBook> getDefaultAddress(){
        //得到用户id
        Long userId = BaseContext.getCurrentId();

        //sql: Select * from address_book where userId = ? and is_default = 1
        LambdaQueryWrapper<AddressBook> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AddressBook::getUserId,userId);
        lqw.eq(AddressBook::getIsDefault,1);
        AddressBook address = addressBookService.getOne(lqw);

        return R.success(address);
    }



}
