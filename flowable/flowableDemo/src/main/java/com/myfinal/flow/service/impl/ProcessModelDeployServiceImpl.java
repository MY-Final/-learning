package com.myfinal.flow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.myfinal.flow.dto.ProcessUploadDeployRequestDTO;
import com.myfinal.flow.pojo.WfProcessDeployLog;
import com.myfinal.flow.pojo.WfProcessModel;
import com.myfinal.flow.pojo.WfProcessModelVersion;
import com.myfinal.flow.service.ProcessModelDeployService;
import com.myfinal.flow.service.WfProcessDeployLogService;
import com.myfinal.flow.service.WfProcessModelService;
import com.myfinal.flow.service.WfProcessModelVersionService;
import com.myfinal.flow.vo.ProcessDeployResultVO;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HexFormat;
import java.util.Objects;

@Service
/**
 * 流程模型上传并自动部署实现。
 *
 * <p>核心流程：校验文件 -> 保存版本存档 -> 调用 Flowable 部署 -> 回写部署结果 -> 写部署日志。</p>
 */
public class ProcessModelDeployServiceImpl implements ProcessModelDeployService {

    private static final Logger log = LoggerFactory.getLogger(ProcessModelDeployServiceImpl.class);

    private final WfProcessModelService processModelService;
    private final WfProcessModelVersionService modelVersionService;
    private final WfProcessDeployLogService deployLogService;
    private final RepositoryService repositoryService;

    public ProcessModelDeployServiceImpl(WfProcessModelService processModelService, WfProcessModelVersionService modelVersionService, WfProcessDeployLogService deployLogService, RepositoryService repositoryService) {
        this.processModelService = processModelService;
        this.modelVersionService = modelVersionService;
        this.deployLogService = deployLogService;
        this.repositoryService = repositoryService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 上传 BPMN 并自动部署。
     *
     * <p>该方法与业务表写入、部署状态回写、部署日志写入保持在同一事务语义下，
     * 发生异常时会记录失败日志并抛出异常。</p>
     */
    public ProcessDeployResultVO uploadAndDeploy(ProcessUploadDeployRequestDTO requestDTO) {
        MultipartFile file = requestDTO.getFile();
        String modelKey = requestDTO.getModelKey();
        String modelName = requestDTO.getModelName();
        String category = requestDTO.getCategory();
        String tenantId = requestDTO.getTenantId();
        String operator = requestDTO.getOperator();

        validateUploadFile(file);
        final String fileName = Objects.requireNonNull(file.getOriginalFilename(), "fileName cannot be null");
        log.info("开始处理流程上传, fileName={}", fileName);

        final String xml;
        try {
            xml = new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("读取上传文件失败", e);
        }
        if (!StringUtils.hasText(xml)) {
            throw new IllegalArgumentException("流程文件内容不能为空");
        }

        final String normalizedTenantId = normalizeBlankToNull(tenantId);
        final String normalizedCategory = normalizeBlankToNull(category);
        final String realModelKey = StringUtils.hasText(modelKey) ? modelKey.trim() : parseModelKeyFromFileName(fileName);
        final String realModelName = StringUtils.hasText(modelName) ? modelName.trim() : realModelKey;
        final String uploader = StringUtils.hasText(operator) ? operator.trim() : "system";
        // 用内容摘要做版本内容指纹，后续可用于去重或审计对比。
        final String sha256 = sha256(xml);
        log.info("流程上传参数已解析, modelKey={}, tenantId={}, operator={}", realModelKey, normalizedTenantId, uploader);

        WfProcessModel model = findOrCreateModel(realModelKey, realModelName, normalizedCategory, normalizedTenantId, uploader);
        final int nextVersion = resolveNextVersion(model.getId());

        WfProcessModelVersion modelVersion = new WfProcessModelVersion();
        modelVersion.setModelId(model.getId());
        modelVersion.setVersion(nextVersion);
        modelVersion.setFileName(fileName);
        modelVersion.setBpmnXml(xml);
        modelVersion.setContentSha256(sha256);
        modelVersion.setSourceType("UPLOAD");
        modelVersion.setUploadUser(uploader);
        modelVersion.setUploadTime(new Date());
        modelVersion.setDeployStatus(0);
        modelVersionService.save(modelVersion);
        log.info("流程版本存档成功, modelId={}, version={}", model.getId(), nextVersion);

        try {
            // 将上传 XML 作为部署资源直接提交给 Flowable。
            Deployment deployment = repositoryService.createDeployment().name(realModelName + "_v" + nextVersion).key(realModelKey).addString(fileName, xml).deploy();

            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).latestVersion().singleResult();

            modelVersion.setDeployStatus(1);
            modelVersion.setDeploymentId(deployment.getId());
            modelVersion.setProcessDefinitionId(processDefinition == null ? null : processDefinition.getId());
            modelVersion.setDeployedAt(LocalDateTime.now());
            modelVersion.setDeployError(null);
            modelVersionService.updateById(modelVersion);

            model.setLatestVersion(nextVersion);
            model.setUpdatedBy(uploader);
            model.setUpdatedAt(LocalDateTime.now());
            processModelService.updateById(model);

            saveDeployLog(modelVersion.getId(), "AUTO_DEPLOY", 1, deployment.getId(), processDefinition == null ? null : processDefinition.getId(), "上传并自动部署成功", uploader);
            log.info("流程部署成功, modelKey={}, version={}, deploymentId={}, processDefinitionId={}",
                    realModelKey, nextVersion, deployment.getId(), processDefinition == null ? null : processDefinition.getId());

            ProcessDeployResultVO resultVO = new ProcessDeployResultVO();
            resultVO.setModelId(model.getId());
            resultVO.setModelKey(realModelKey);
            resultVO.setVersion(nextVersion);
            resultVO.setDeployStatus(1);
            resultVO.setDeploymentId(deployment.getId());
            resultVO.setProcessDefinitionId(processDefinition == null ? null : processDefinition.getId());
            return resultVO;
        } catch (Exception e) {
            log.error("上传自动部署失败, modelKey={}, version={}, fileName={}", realModelKey, nextVersion, fileName, e);

            modelVersion.setDeployStatus(2);
            modelVersion.setDeployError(e.getMessage());
            modelVersion.setDeployedAt(LocalDateTime.now());
            modelVersionService.updateById(modelVersion);

            saveDeployLog(modelVersion.getId(), "AUTO_DEPLOY", 0, null, null, e.getMessage(), uploader);
            throw new IllegalStateException("流程部署失败: " + e.getMessage(), e);
        }
    }

    /**
     * 按 modelKey + tenantId 查询模型，不存在则创建。
     */
    private WfProcessModel findOrCreateModel(String modelKey, String modelName, String category, String tenantId, String operator) {
        LambdaQueryWrapper<WfProcessModel> query = new LambdaQueryWrapper<>();
        query.eq(WfProcessModel::getModelKey, modelKey);
        if (StringUtils.hasText(tenantId)) {
            query.eq(WfProcessModel::getTenantId, tenantId);
        } else {
            query.isNull(WfProcessModel::getTenantId);
        }
        query.last("limit 1");
        WfProcessModel model = processModelService.getOne(query, false);
        if (model != null) {
            return model;
        }

        WfProcessModel newModel = new WfProcessModel();
        newModel.setModelKey(modelKey);
        newModel.setModelName(modelName);
        newModel.setCategory(category);
        newModel.setTenantId(tenantId);
        newModel.setLatestVersion(0);
        newModel.setStatus(1);
        newModel.setCreatedBy(operator);
        newModel.setCreatedAt(LocalDateTime.now());
        newModel.setUpdatedBy(operator);
        newModel.setUpdatedAt(LocalDateTime.now());
        processModelService.save(newModel);
        return newModel;
    }

    /**
     * 获取下一个版本号（从 1 开始递增）。
     */
    private int resolveNextVersion(Long modelId) {
        LambdaQueryWrapper<WfProcessModelVersion> query = new LambdaQueryWrapper<>();
        query.eq(WfProcessModelVersion::getModelId, modelId).orderByDesc(WfProcessModelVersion::getVersion).last("limit 1");
        WfProcessModelVersion latest = modelVersionService.getOne(query, false);
        if (latest == null || latest.getVersion() == null) {
            return 1;
        }
        return latest.getVersion() + 1;
    }

    /**
     * 记录部署动作日志（成功/失败都写入）。
     */
    private void saveDeployLog(Long modelVersionId, String actionType, int success, String deploymentId, String processDefinitionId, String message, String operator) {
        WfProcessDeployLog deployLog = new WfProcessDeployLog();
        deployLog.setModelVersionId(modelVersionId);
        deployLog.setActionType(actionType);
        deployLog.setSuccess(success);
        deployLog.setDeploymentId(deploymentId);
        deployLog.setProcessDefinitionId(processDefinitionId);
        deployLog.setMessage(message);
        deployLog.setTriggeredBy(operator);
        deployLog.setTriggeredAt(LocalDateTime.now());
        deployLogService.save(deployLog);
    }

    /**
     * 校验上传文件基础信息与后缀。
     */
    private void validateUploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        String fileName = file.getOriginalFilename();
        if (!StringUtils.hasText(fileName)) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        String lowerName = fileName.toLowerCase();
        if (!(lowerName.endsWith(".bpmn20.xml") || lowerName.endsWith(".bpmn"))) {
            throw new IllegalArgumentException("仅支持 .bpmn 或 .bpmn20.xml 文件");
        }
    }

    /**
     * 未显式传 modelKey 时，从文件名提取流程 key。
     */
    private String parseModelKeyFromFileName(String fileName) {
        String key = fileName.trim();
        String lowerName = key.toLowerCase();
        if (lowerName.endsWith(".bpmn20.xml")) {
            key = key.substring(0, key.length() - ".bpmn20.xml".length());
        } else if (lowerName.endsWith(".bpmn")) {
            key = key.substring(0, key.length() - ".bpmn".length());
        }
        if (!StringUtils.hasText(key)) {
            throw new IllegalArgumentException("无法从文件名解析流程Key，请显式传入 modelKey");
        }
        return key;
    }

    /**
     * 计算文本 SHA-256 摘要。
     */
    private String sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 算法不可用", e);
        }
    }

    private String normalizeBlankToNull(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        return text.trim();
    }
}
