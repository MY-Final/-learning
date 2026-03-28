package com.myfinal.flow;

import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.Deployment;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class FlowableDemoApplicationTests {

    @Autowired
    ProcessEngine processEngine;

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    TaskService taskService;

    // 人物名称 张三和李四
    private static final String zhangSan = "张三";
    private static final String lisi = "李四";

    /***
     * 测试 Spring 集成
     */
    @Test
    void deployFlowSpring() {
        System.out.println("✅ Flowable ProcessEngine 创建成功！");
        System.out.println("流程引擎: " + processEngine);
//        RepositoryService repositoryService = processEngine.getRepositoryService();
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("process/01-基础篇/FirstFlow.bpmn20.xml")
                .name("第一个流程图")
                .deploy();
        System.out.println("✅ 流程部署成功！");
        System.out.println("deployment = " + deployment);
    }

    /**
     * 启动流程
     */
    @Test
    void startFlow() {
        // 获取流程实例 ID
        String processInstanceId = "FirstFlow:1:73d1b143-2a6d-11f1-bf90-005056c00008";
        runtimeService.startProcessInstanceById(processInstanceId);
    }

    /**
     * 查询流程
     */
    @Test
    void findFlow() {
        // 获取流程实例 ID
        List<Task> list = taskService.createTaskQuery()
                .taskAssignee(zhangSan)
                .list();
        for (Task task : list) {
            System.out.println("任务ID: " + task.getId());
        }
    }

    /**
     * 审批任务
     */
    @Test
    void completeTask() {
        taskService.complete("d3c45d80-2a8e-11f1-9fb4-005056c00008");
        System.out.println("✅ 任务完成！");
    }
}
