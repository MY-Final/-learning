package com.myfinal.flow.bean;

import org.springframework.stereotype.Component;

@Component
public class myBean {
    public String getAssignee(){
        System.out.println("MyBean.getAssignee...执行了");
        return "王五";
    }
}
