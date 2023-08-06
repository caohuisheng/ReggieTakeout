package com.itheima.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/*
全局异常处理
 */
@ControllerAdvice(annotations = {RestController.class, Controller.class})   //设置要拦截的类
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {
    /*
    添加重复异常处理方法
     */
    @ExceptionHandler
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex){ //处理SQLIntegrityConstraintViolationException异常
        log.error(ex.getMessage());

        //数据库已存在该用户
        if(ex.getMessage().contains("Duplicate entry")){
            String[] split = ex.getMessage().split(" ");
            String msg = split[2] + "已存在";
            return R.error(msg);
        }

        return R.error("未知错误");
    }

    /**
     * 分类有关联异常处理
     * @param ex
     * @return
     */
    @ExceptionHandler
    public R<String> exceptionHandler(CustomException ex){
        log.error(ex.getMessage());

        return R.error(ex.getMessage());
    }
}
