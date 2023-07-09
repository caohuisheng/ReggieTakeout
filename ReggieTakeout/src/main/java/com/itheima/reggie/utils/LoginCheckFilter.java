package com.itheima.reggie.utils;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/*
检查用户是否已经登陆
 */
@WebFilter(filterName = "LoginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {

    //路径匹配器
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //1.获取本次请求的URI
        String requestURI = request.getRequestURI();

        log.info("拦截到请求：{}",requestURI);

        //定义不需要处理的请求路径
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login"
        };

        //2.判断本次请求是否需要处理
        boolean check = check(urls,requestURI);
        //如果不需要处理，直接放行
        if(check){
            log.info("本次请求{}不需要处理",requestURI);
            filterChain.doFilter(request,response);
            return;
        }

        //3-1.判断后台登陆状态，如果已登陆，直接放行
        if(request.getSession().getAttribute("employee") != null){
            log.info("用户已登陆，用户id为:{}",request.getSession().getAttribute("employee"));

//            long id = Thread.currentThread().getId();
//            log.info("线程id：{}",id);
            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);

            filterChain.doFilter(request,response);
            return;
        }

        //3-2.判断客户端登陆状态，如果已登陆，直接放行
        if(request.getSession().getAttribute("user") != null){
            log.info("用户已登陆，用户id为:{}",request.getSession().getAttribute("user"));

//            long id = Thread.currentThread().getId();
//            log.info("线程id：{}",id);
            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request,response);
            return;
        }

        log.info("用户未登录");
        //4.如果未登录则返回未登陆结果，通过输出流向客户端响应数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
    }

    /*
    路径匹配
     */
    public boolean check(String[] urls,String requestURI){
        for(String url:urls){
            boolean match = PATH_MATCHER.match(url, requestURI);
            if(match) return true;
        }
        return false;
    }


}
