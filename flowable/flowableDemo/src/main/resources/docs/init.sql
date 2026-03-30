-- 1) 流程模型主表（按业务 key 管理）
CREATE TABLE wf_process_model
(
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    model_key      VARCHAR(128) NOT NULL COMMENT '流程唯一业务Key，如 Example01',
    model_name     VARCHAR(255) NOT NULL COMMENT '流程名称',
    category       VARCHAR(128) NULL,
    tenant_id      VARCHAR(64)  NULL,
    latest_version INT          NOT NULL DEFAULT 0,
    status         TINYINT      NOT NULL DEFAULT 1 COMMENT '1启用 0禁用',
    created_by     VARCHAR(64)  NULL,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by     VARCHAR(64)  NULL,
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_model_key_tenant (model_key, tenant_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


-- 2) 流程模型版本表（每次上传一条）
CREATE TABLE wf_process_model_version
(
    id                    BIGINT PRIMARY KEY AUTO_INCREMENT,
    model_id              BIGINT       NOT NULL,
    version               INT          NOT NULL COMMENT '从1递增',
    file_name             VARCHAR(255) NOT NULL COMMENT '如 Example01.bpmn20.xml',
    bpmn_xml              LONGTEXT     NOT NULL COMMENT 'XML文本（也可改成对象存储URL）',
    content_sha256        CHAR(64)     NOT NULL COMMENT '内容摘要用于去重',
    source_type           VARCHAR(32)  NOT NULL DEFAULT 'UPLOAD' COMMENT 'UPLOAD/GIT/API',
    upload_user           VARCHAR(64)  NULL,
    upload_time           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- 部署状态
    deploy_status         TINYINT      NOT NULL DEFAULT 0 COMMENT '0未部署 1成功 2失败',
    deployment_id         VARCHAR(64)  NULL COMMENT 'Flowable ACT_RE_DEPLOYMENT.ID_',
    process_definition_id VARCHAR(128) NULL COMMENT 'Flowable ACT_RE_PROCDEF.ID_',
    deployed_at           DATETIME     NULL,
    deploy_error          TEXT         NULL,

    FOREIGN KEY (model_id) REFERENCES wf_process_model (id),
    UNIQUE KEY uk_model_version (model_id, version),
    KEY idx_sha256 (content_sha256),
    KEY idx_deploy_status (deploy_status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


-- 3) 部署日志表（每次部署动作都记录）
CREATE TABLE wf_process_deploy_log
(
    id                    BIGINT PRIMARY KEY AUTO_INCREMENT,
    model_version_id      BIGINT       NOT NULL,
    action_type           VARCHAR(32)  NOT NULL COMMENT 'AUTO_DEPLOY/MANUAL_RETRY/ROLLBACK',
    success               TINYINT      NOT NULL COMMENT '1成功 0失败',
    deployment_id         VARCHAR(64)  NULL,
    process_definition_id VARCHAR(128) NULL,
    message               TEXT         NULL,
    triggered_by          VARCHAR(64)  NULL,
    triggered_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (model_version_id) REFERENCES wf_process_model_version (id),
    KEY idx_model_version (model_version_id),
    KEY idx_triggered_at (triggered_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;