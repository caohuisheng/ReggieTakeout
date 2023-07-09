package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        //1.将密码进行MD5加密
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2.根据用户名查询
        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(lqw);

        //3.如果用户名不存在
        if(emp == null){
            return R.error("用户不存在");
        }

        //4.如果密码不一致
        if(!emp.getPassword().equals(password)){
            System.out.println(emp.getPassword());
            System.out.println(password);
            return R.error("登陆失败");
        }

        //5.如果用户被禁用
        if(emp.getStatus() == 0){
            return R.error("账号已被禁用");
        }

        //6.登陆成功,将员工id存入session
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }

    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //情况session中保存的当前员工的id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /*
    新增员工
     */
    @PostMapping
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){
        log.info("新增员工：{}",employee.toString());

        //设置初始密码，并进行MD5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        /*employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        //获取当前登陆用户id
        Long empId = (Long) request.getSession().getAttribute("employee");

        employee.setCreateUser(empId);
        employee.setUpdateUser(empId);*/

        employeeService.save(employee);
        return R.success("新增员工成功");
    }

    /*
    分页查询
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        log.info("page = {},pagesize= {},name = {}",page,pageSize,name);

        //1.构造分页构造器
        Page<Employee> pageInfo = new Page<>(page,pageSize);

        //2.构造条件构造器
        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper<Employee>();
        lqw.like(!StringUtils.isEmpty(name),Employee::getUsername,name);
        lqw.orderByDesc(Employee::getUpdateTime);

        //3.执行查询
        employeeService.page(pageInfo,lqw);
        System.out.println(pageInfo.getRecords());

        return R.success(pageInfo);
        //pageInfo.getTotal();

    }

    /*
    修改员工信息
     */
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
        log.info(employee.toString());

        long id = Thread.currentThread().getId();
        log.info("线程id：{}",id);

        //获取修改者的id
        /*Long empId = (Long) request.getSession().getAttribute("employee");
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(empId);*/
        employeeService.updateById(employee);

        return R.success("员工信息修改成功");
    }

    /*
    根据员工id查询信息
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("根据员工id查询信息...");
        Employee employee = employeeService.getById(id);
        return R.success(employee);
    }

}
