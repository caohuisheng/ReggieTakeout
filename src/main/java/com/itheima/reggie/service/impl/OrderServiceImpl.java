package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.mapper.OrderMapper;
import com.itheima.reggie.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    public void submit(Orders order){
        //获得当前用户id
        Long userId = BaseContext.getCurrentId();
        User user = userService.getById(userId);

        //得到用户购物车数据
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> shoppingcarts = shoppingCartService.list(lqw);

        //得到订单地址信息
        AddressBook addressBook = addressBookService.getById(order.getAddressBookId());

        long orderId = IdWorker.getId(); //订单号

        //设置订单的其它信息
        order.setUserName(user.getName());
        order.setId(orderId);
        order.setUserId(userId);
        order.setOrderTime(LocalDateTime.now());
        order.setCheckoutTime(LocalDateTime.now());
        order.setPayMethod(1);
        String address = (addressBook.getProvinceName() == null ? "":addressBook.getProvinceName())+
                (addressBook.getCityName() == null ? "":addressBook.getCityName()) +
                (addressBook.getDistrictName() == null ? "":addressBook.getDistrictName())+
                (addressBook.getDetail() == null ?"":addressBook.getDetail());
        order.setAddress(address);
        order.setPhone(addressBook.getConsignee());
        order.setConsignee(addressBook.getConsignee());
        order.setUserName(user.getName());

        AtomicInteger amount = new AtomicInteger(0); //订单总金额

        //将购物车的数据准换为订单详情数据
        List<OrderDetail> orderDetails = shoppingcarts.stream().map(item -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId((item.getDishId()));
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            orderDetail.setDishFlavor(item.getDishFlavor());
            amount.addAndGet(item.getAmount().multiply(item.getAmount()).intValue());
            return orderDetail;
        }).collect(Collectors.toList());

        order.setAmount(new BigDecimal(amount.get()));

        this.save(order);
        orderDetailService.saveBatch(orderDetails);
        shoppingCartService.remove(lqw);
    }
}
