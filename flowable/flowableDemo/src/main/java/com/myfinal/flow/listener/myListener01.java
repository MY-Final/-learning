package com.myfinal.flow.listener;

import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.task.service.delegate.TaskListener;

public class myListener01 implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        System.out.println("监听器执行了...");
        System.out.println("delegateTask = " + delegateTask);
        System.out.println("delegateTask.getEventName() = " + delegateTask.getEventName());
        if(EVENTNAME_CREATE.equals(delegateTask.getEventName())){
            // 用户节点创建
            delegateTask.setAssignee("湘湘");
        }
    }
}
