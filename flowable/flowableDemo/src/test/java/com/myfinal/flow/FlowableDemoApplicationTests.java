package com.myfinal.flow;

import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                .addClasspathResource("process/01-基础篇/Example02.bpmn20.xml")
                .name("监听器")
                .deploy();
        System.out.println("✅ 流程部署成功！");
        System.out.println("deployment = " + deployment);
    }

    /**
     * 启动流程
     */
    @Test
    void startFlow() {
        // 在启动流程实例的时候绑定表达式的值
        Map<String, Object> variables = new HashMap<>();
        variables.put("assgin2", zhangSan);
        // 获取流程实例 ID
        String processInstanceId = "Example02:1:0801a1ff-2ac3-11f1-9e1f-005056c00008";
        runtimeService.startProcessInstanceById(processInstanceId,variables );
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
        if (list.size() > 0){
            for (Task task : list) {
                System.out.println("任务ID: " + task.getId());
            }
        }
        System.out.println("无任务实例");
    }

    /**
     * 审批任务
     */
    @Test
    void completeTask() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("assgin1", lisi);
//        taskService.complete("8b6adde8-2ac0-11f1-98df-005056c00008",variables);
        taskService.complete("bcace4c4-2ac4-11f1-9488-005056c00008");
        System.out.println("✅ 任务完成！");
    }

    /**
     * 流程挂起，激活
     */
    @Test
    void suspendAndActivate() {
        String processDefinitionId = "FirstFlow:1:73d1b143-2a6d-11f1-bf90-005056c00008";
        // 做流程的挂起和激活操作--->针对的流程定义
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId)
                .singleResult();
        // 获取当前的流程定义的状态
        boolean suspended = processDefinition.isSuspended(); // 获取是否被挂起
        if (suspended) {
            // 表示挂起 ---> 需要激活流程
            System.out.println("激活流程");
            repositoryService.activateProcessDefinitionById(processDefinitionId);
        } else {
            // 表示激活 ---> 挂起流程
            System.out.println("挂起流程");
            repositoryService.suspendProcessDefinitionById(processDefinitionId);
        }
    }

    /**
     * 挂起流程实例
     */
    @Test
    void suspendProcessInstance() {

        // 挂起流程实例
        runtimeService.suspendProcessInstanceById("06804572-2ab6-11f1-942e-005056c00008");
        // 激活流程实例
//        runtimeService.activateProcessInstanceById("06804572-2ab6-11f1-942e-005056c00008");
    }
}
