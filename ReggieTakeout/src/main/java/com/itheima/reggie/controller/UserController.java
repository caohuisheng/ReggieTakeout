package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.SMSUtils;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 发送手机验证码
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user,HttpSession session){
        //获取手机号
        String phone = user.getPhone();

        if(StringUtils.isNotEmpty(phone)){
            //生成随机的四位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code:{}",code);

            //发送验证码短信给客户端
            //SMSUtils.sendMessage("瑞吉外卖","",phone,code);

            //将生成的验证码保存到Session
            session.setAttribute(phone,code);
        }

        return R.success("发送验证码成功");
    }

    /**
     * 登陆
     * @return
     */
    @RequestMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession httpSession){
        log.info(map.toString());

        //得到手机号和验证码
        String phone = map.get("phone").toString();
        String code = map.get("code").toString();

        //得到Session中的验证码
        Object codeInSession = httpSession.getAttribute(phone);

        //判断Session中的验证码与输入的是否相同
        if(codeInSession != null && codeInSession.equals(code)){
            //根据手机号查询数据库中对应的的用户
            LambdaQueryWrapper<User> lqw = new LambdaQueryWrapper<User>();
            lqw.eq(User::getPhone,phone);
            User user = userService.getOne(lqw);
            //判断改用的是否存在
            if(user == null){
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            //登陆成功，将用户信息存入session
            httpSession.setAttribute("user",user.getId());
            return R.success(user);
        }

        return R.error("登陆失败");
    }
}
