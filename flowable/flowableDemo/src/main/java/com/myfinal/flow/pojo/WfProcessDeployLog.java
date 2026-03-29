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
 * @TableName wf_process_deploy_log
 */
@TableName(value ="wf_process_deploy_log")
@Data
public class WfProcessDeployLog {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Long modelVersionId;

    /**
     * AUTO_DEPLOY/MANUAL_RETRY/ROLLBACK
     */
    private String actionType;

    /**
     * 1成功 0失败
     */
    private Integer success;

    /**
     * 
     */
    private String deploymentId;

    /**
     * 
     */
    private String processDefinitionId;

    /**
     * 
     */
    private String message;

    /**
     * 
     */
    private String triggeredBy;

    /**
     * 
     */
    private LocalDateTime triggeredAt;

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
        WfProcessDeployLog other = (WfProcessDeployLog) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getModelVersionId() == null ? other.getModelVersionId() == null : this.getModelVersionId().equals(other.getModelVersionId()))
            && (this.getActionType() == null ? other.getActionType() == null : this.getActionType().equals(other.getActionType()))
            && (this.getSuccess() == null ? other.getSuccess() == null : this.getSuccess().equals(other.getSuccess()))
            && (this.getDeploymentId() == null ? other.getDeploymentId() == null : this.getDeploymentId().equals(other.getDeploymentId()))
            && (this.getProcessDefinitionId() == null ? other.getProcessDefinitionId() == null : this.getProcessDefinitionId().equals(other.getProcessDefinitionId()))
            && (this.getMessage() == null ? other.getMessage() == null : this.getMessage().equals(other.getMessage()))
            && (this.getTriggeredBy() == null ? other.getTriggeredBy() == null : this.getTriggeredBy().equals(other.getTriggeredBy()))
            && (this.getTriggeredAt() == null ? other.getTriggeredAt() == null : this.getTriggeredAt().equals(other.getTriggeredAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getModelVersionId() == null) ? 0 : getModelVersionId().hashCode());
        result = prime * result + ((getActionType() == null) ? 0 : getActionType().hashCode());
        result = prime * result + ((getSuccess() == null) ? 0 : getSuccess().hashCode());
        result = prime * result + ((getDeploymentId() == null) ? 0 : getDeploymentId().hashCode());
        result = prime * result + ((getProcessDefinitionId() == null) ? 0 : getProcessDefinitionId().hashCode());
        result = prime * result + ((getMessage() == null) ? 0 : getMessage().hashCode());
        result = prime * result + ((getTriggeredBy() == null) ? 0 : getTriggeredBy().hashCode());
        result = prime * result + ((getTriggeredAt() == null) ? 0 : getTriggeredAt().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", modelVersionId=").append(modelVersionId);
        sb.append(", actionType=").append(actionType);
        sb.append(", success=").append(success);
        sb.append(", deploymentId=").append(deploymentId);
        sb.append(", processDefinitionId=").append(processDefinitionId);
        sb.append(", message=").append(message);
        sb.append(", triggeredBy=").append(triggeredBy);
        sb.append(", triggeredAt=").append(triggeredAt);
        sb.append("]");
        return sb.toString();
    }
}