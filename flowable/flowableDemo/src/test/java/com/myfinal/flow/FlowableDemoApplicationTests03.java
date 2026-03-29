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
class FlowableDemoApplicationTests03 {

    // 人物名称 张三和李四
    private static final String zhangSan = "张三";
    private static final String lisi = "李四";
    @Autowired
    ProcessEngine processEngine;
    @Autowired
    RepositoryService repositoryService;
    @Autowired
    RuntimeService runtimeService;
    @Autowired
    TaskService taskService;

    /***
     * 测试 Spring 集成
     */
    @Test
    void deployFlowSpring() {
        System.out.println("✅ Flowable ProcessEngine 创建成功！");
        System.out.println("流程引擎: " + processEngine);
//        RepositoryService repositoryService = processEngine.getRepositoryService();
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("process/01-基础篇/holidaydemo2.bpmn20.xml")
                .name("候选人案例")
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
        variables.put("candidate1", "张三");
        variables.put("candidate2", "李四");
        variables.put("candidate3", "王五");
        // 获取流程实例 ID
        String processInstanceId = "holidaydemo2:1:8bb61432-2b36-11f1-a73f-005056c00008";
        runtimeService.startProcessInstanceById(processInstanceId, variables);
    }


    /**
     * 根据候选人查询待办信息
     * 候选人还不是审批人
     * 候选人需要通过拾取的操作，把候选人变为审批人
     * 多个候选人只有一个变为审批人 拾取的操作
     * 审批人如果不想审批，可以归还。从审批人变为候选人
     */
    @Test
    void climTask() {
        List<Task> list = taskService.createTaskQuery()
                .taskCandidateUser("final")
                .list();
        if (list.size() > 0) {
            for (Task task : list) {
                System.out.println("任务ID: " + task.getId());
                // 拾取的动作，把张三变为审批人
                taskService.claim(task.getId(), "final");
            }
        }
    }

    /**
     * 归还操作
     */
    @Test
    void returnTask() {
        List<Task> list = taskService.createTaskQuery()
                .taskAssignee("final")
                .list();
        if (list.size() > 0) {
            for (Task task : list) {
                System.out.println("任务ID: " + task.getId());
                System.out.println("归还操作");
                // 归还操作，审批人回到候选人
                taskService.unclaim(task.getId());
            }
        }
    }

    /**
     * 指派操作
     * 指派一个用户去操作
     */
    @Test
    void assignTask() {
        List<Task> list = taskService.createTaskQuery()
                .taskAssignee("final")
                .list();
        if (list.size() > 0) {
            for (Task task : list) {
                System.out.println("任务ID: " + task.getId());
                // 指派操作
                taskService.setAssignee(task.getId(), "myfinal");
            }
        }
    }


    /**
     * 审批任务
     */
    @Test
    void completeTask() {
        List<Task> list = taskService.createTaskQuery()
                .taskAssignee(zhangSan)
                .list(); // 返回给前端，登录用户看到多条待办，选中其中一条处理
//        Map<String, Object> variables = new HashMap<>();
//        variables.put("candidate4", "final");
//        variables.put("candidate5", "may");
        if (list.size() > 0) {
            for (Task task : list) {
                taskService.complete("e298b43a-2b37-11f1-9444-005056c00008");
                System.out.println("任务ID: " + task.getId());
            }
        }
    }

    @Test
    void completeTask1() {
        taskService.complete("e298b43a-2b37-11f1-9444-005056c00008");

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
