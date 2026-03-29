package com.myfinal.flow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

@Schema(description = "流程上传并部署请求")
public class ProcessUploadDeployRequestDTO {

    @Schema(description = "BPMN 文件", requiredMode = Schema.RequiredMode.REQUIRED, type = "string", format = "binary")
    private MultipartFile file;

    @Schema(description = "流程业务 Key，不传则从文件名解析")
    private String modelKey;

    @Schema(description = "流程名称，不传默认使用 modelKey")
    private String modelName;

    @Schema(description = "流程分类")
    private String category;

    @Schema(description = "租户ID")
    private String tenantId;

    @Schema(description = "操作人")
    private String operator;

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public String getModelKey() {
        return modelKey;
    }

    public void setModelKey(String modelKey) {
        this.modelKey = modelKey;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }
}
