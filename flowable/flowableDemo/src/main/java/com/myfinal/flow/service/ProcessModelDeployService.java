package com.myfinal.flow.service;

import com.myfinal.flow.dto.ProcessUploadDeployRequestDTO;
import com.myfinal.flow.vo.ProcessDeployResultVO;

public interface ProcessModelDeployService {

    ProcessDeployResultVO uploadAndDeploy(ProcessUploadDeployRequestDTO requestDTO);
}
