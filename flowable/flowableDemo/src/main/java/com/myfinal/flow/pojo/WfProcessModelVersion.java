package com.myfinal.flow.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName wf_process_model_version
 */
@TableName(value ="wf_process_model_version")
@Data
public class WfProcessModelVersion {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Long modelId;

    /**
     * 从1递增
     */
    private Integer version;

    /**
     * 如 Example01.bpmn20.xml
     */
    private String fileName;

    /**
     * XML文本（也可改成对象存储URL）
     */
    private String bpmnXml;

    /**
     * 内容摘要用于去重
     */
    private String contentSha256;

    /**
     * UPLOAD/GIT/API
     */
    private String sourceType;

    /**
     * 
     */
    private String uploadUser;

    /**
     * 
     */
    private Date uploadTime;

    /**
     * 0未部署 1成功 2失败
     */
    private Integer deployStatus;

    /**
     * Flowable ACT_RE_DEPLOYMENT.ID_
     */
    private String deploymentId;

    /**
     * Flowable ACT_RE_PROCDEF.ID_
     */
    private String processDefinitionId;

    /**
     * 
     */
    private LocalDateTime deployedAt;

    /**
     * 
     */
    private String deployError;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        WfProcessModelVersion other = (WfProcessModelVersion) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getModelId() == null ? other.getModelId() == null : this.getModelId().equals(other.getModelId()))
            && (this.getVersion() == null ? other.getVersion() == null : this.getVersion().equals(other.getVersion()))
            && (this.getFileName() == null ? other.getFileName() == null : this.getFileName().equals(other.getFileName()))
            && (this.getBpmnXml() == null ? other.getBpmnXml() == null : this.getBpmnXml().equals(other.getBpmnXml()))
            && (this.getContentSha256() == null ? other.getContentSha256() == null : this.getContentSha256().equals(other.getContentSha256()))
            && (this.getSourceType() == null ? other.getSourceType() == null : this.getSourceType().equals(other.getSourceType()))
            && (this.getUploadUser() == null ? other.getUploadUser() == null : this.getUploadUser().equals(other.getUploadUser()))
            && (this.getUploadTime() == null ? other.getUploadTime() == null : this.getUploadTime().equals(other.getUploadTime()))
            && (this.getDeployStatus() == null ? other.getDeployStatus() == null : this.getDeployStatus().equals(other.getDeployStatus()))
            && (this.getDeploymentId() == null ? other.getDeploymentId() == null : this.getDeploymentId().equals(other.getDeploymentId()))
            && (this.getProcessDefinitionId() == null ? other.getProcessDefinitionId() == null : this.getProcessDefinitionId().equals(other.getProcessDefinitionId()))
            && (this.getDeployedAt() == null ? other.getDeployedAt() == null : this.getDeployedAt().equals(other.getDeployedAt()))
            && (this.getDeployError() == null ? other.getDeployError() == null : this.getDeployError().equals(other.getDeployError()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getModelId() == null) ? 0 : getModelId().hashCode());
        result = prime * result + ((getVersion() == null) ? 0 : getVersion().hashCode());
        result = prime * result + ((getFileName() == null) ? 0 : getFileName().hashCode());
        result = prime * result + ((getBpmnXml() == null) ? 0 : getBpmnXml().hashCode());
        result = prime * result + ((getContentSha256() == null) ? 0 : getContentSha256().hashCode());
        result = prime * result + ((getSourceType() == null) ? 0 : getSourceType().hashCode());
        result = prime * result + ((getUploadUser() == null) ? 0 : getUploadUser().hashCode());
        result = prime * result + ((getUploadTime() == null) ? 0 : getUploadTime().hashCode());
        result = prime * result + ((getDeployStatus() == null) ? 0 : getDeployStatus().hashCode());
        result = prime * result + ((getDeploymentId() == null) ? 0 : getDeploymentId().hashCode());
        result = prime * result + ((getProcessDefinitionId() == null) ? 0 : getProcessDefinitionId().hashCode());
        result = prime * result + ((getDeployedAt() == null) ? 0 : getDeployedAt().hashCode());
        result = prime * result + ((getDeployError() == null) ? 0 : getDeployError().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", modelId=").append(modelId);
        sb.append(", version=").append(version);
        sb.append(", fileName=").append(fileName);
        sb.append(", bpmnXml=").append(bpmnXml);
        sb.append(", contentSha256=").append(contentSha256);
        sb.append(", sourceType=").append(sourceType);
        sb.append(", uploadUser=").append(uploadUser);
        sb.append(", uploadTime=").append(uploadTime);
        sb.append(", deployStatus=").append(deployStatus);
        sb.append(", deploymentId=").append(deploymentId);
        sb.append(", processDefinitionId=").append(processDefinitionId);
        sb.append(", deployedAt=").append(deployedAt);
        sb.append(", deployError=").append(deployError);
        sb.append("]");
        return sb.toString();
    }
}