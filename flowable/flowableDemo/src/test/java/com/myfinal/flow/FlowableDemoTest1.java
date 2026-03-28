package com.myfinal.flow;

import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.flowable.engine.repository.Deployment;
import org.junit.jupiter.api.Test;

public class FlowableDemoTest1 {

    @Test
    void deployFlow() {
        System.out.println("---------------------- 开始创建 Flowable ProcessEngine ----------------------");

        // 使用 StandaloneProcessEngineConfiguration（适合非 Spring 环境测试）
        ProcessEngineConfiguration cfg = new StandaloneProcessEngineConfiguration()
                .setJdbcUrl("jdbc:mysql://localhost:3306/flowable_learning?characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai")
                .setJdbcDriver("com.mysql.cj.jdbc.Driver")
                .setJdbcUsername("root")
                .setJdbcPassword("lxy666666")
                .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);  // 自动创建/更新表

        // 构建流程引擎（这一步会连接数据库并初始化 Flowable 的 20+ 张表）
        ProcessEngine processEngine = cfg.buildProcessEngine();

        System.out.println("✅ Flowable ProcessEngine 创建成功！");
        System.out.println("流程引擎名称: " + processEngine.getName());
        // 开始部署
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Deployment deployment =  repositoryService.createDeployment()
                .addClasspathResource("process/01-基础篇/FirstFlow.bpmn20.xml")
                .name("第一个流程图")
                .deploy();
        System.out.println("✅ 流程部署成功！");
        System.out.println("deployment = " + deployment);

        // 不要立即关闭，后面部署流程时还会用到
        // processEngine.close();   // 测试完可以关闭
    }
}