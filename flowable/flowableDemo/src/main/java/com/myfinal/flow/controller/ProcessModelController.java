package com.myfinal.flow.controller;

import com.myfinal.flow.dto.ProcessUploadDeployRequestDTO;
import com.myfinal.flow.service.ProcessModelDeployService;
import com.myfinal.flow.vo.ApiResponse;
import com.myfinal.flow.vo.ProcessDeployResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "流程模型管理")
@RestController
@RequestMapping("/api/process-models")
public class ProcessModelController {

    private final ProcessModelDeployService processModelDeployService;

    public ProcessModelController(ProcessModelDeployService processModelDeployService) {
        this.processModelDeployService = processModelDeployService;
    }

    /**
     * 上传 BPMN 文件并自动部署流程定义。
     */
    @Operation(summary = "上传 BPMN 并自动部署", description = "上传 .bpmn/.bpmn20.xml 后自动完成存档和 Flowable 部署")
    @PostMapping(value = "/upload-deploy", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ProcessDeployResultVO> uploadAndDeploy(@ModelAttribute ProcessUploadDeployRequestDTO requestDTO) {
        ProcessDeployResultVO resultVO = processModelDeployService.uploadAndDeploy(requestDTO);
        return ApiResponse.ok("上传并部署成功", resultVO);
    }
}
