package com.RQ.tuyunthinktank;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
// 配置MyBatis的Mapper接口扫描路径
@MapperScan("com.RQ.tuyunthinktank.mapper")
// 启用AspectJ进行面向切面编程，exposeProxy暴露代理对象以便在类内部调用时也能应用切面逻辑
@EnableAspectJAutoProxy(exposeProxy = true)public class TuyunThinkTankApplication {    

    public static void main(String[] args) {
        SpringApplication.run(TuyunThinkTankApplication.class, args);
    }

}
