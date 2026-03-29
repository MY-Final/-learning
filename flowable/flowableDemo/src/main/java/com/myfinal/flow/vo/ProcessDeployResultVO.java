package com.myfinal.flow.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "流程部署结果")
public class ProcessDeployResultVO {

    @Schema(description = "流程模型ID")
    private Long modelId;

    @Schema(description = "流程业务Key")
    private String modelKey;

    @Schema(description = "版本号")
    private Integer version;

    @Schema(description = "部署状态，1=成功")
    private Integer deployStatus;

    @Schema(description = "Flowable部署ID")
    private String deploymentId;

    @Schema(description = "Flowable流程定义ID")
    private String processDefinitionId;

    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public String getModelKey() {
        return modelKey;
    }

    public void setModelKey(String modelKey) {
        this.modelKey = modelKey;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getDeployStatus() {
        return deployStatus;
    }

    public void setDeployStatus(Integer deployStatus) {
        this.deployStatus = deployStatus;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }
}
