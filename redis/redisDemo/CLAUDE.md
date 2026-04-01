# CLAUDE.md

本文档为 Claude Code (claude.ai/code) 在此仓库中工作提供指导。

## 构建与运行命令

```bash
# 构建项目
mvn clean package

# 运行应用
mvn spring-boot:run

# 运行测试
mvn test

# 运行单个测试类
mvn test -Dtest=RedisDemoApplicationTests
```

## 架构概述

Spring Boot 3.5.13 应用 (Java 17)，集成 PingCode API 进行令牌认证，使用 Redis 缓存和 MySQL 持久化。

### 技术栈

- **数据库**: MySQL (`localhost:3306/redis_learning`)，通过 MyBatis-Plus 3.5.11
- **缓存**: Redis (`localhost:6379`)，通过 Spring Data Redis
- **API**: PingCode OAuth2 (`https://open.pingcode.com`)
- **API 文档**: SpringDoc OpenAPI，地址 http://localhost:18700/swagger-ui.html
- **工具库**: FastJSON2、Hutool、Lombok

### 令牌流程

1. `GET /api/pingcode/token` 调用 `PingcodeAuthService.getEnterpriseToken()`
2. 通过 HTTP GET 请求 `/v1/auth/token` 从 PingCode 获取令牌
3. 响应结果缓存到 Redis，键为 `pingcode:token:enterprise`
4. 令牌日志保存到 `pingcode_token_log` 表（含哈希和前缀处理以保障安全）

### 核心组件

- `PingcodeController` - 令牌接口端点，含 Redis 缓存
- `PingcodeTokenController` - 令牌日志的 CRUD 操作
- `PingcodeAuthService` - OAuth2 令牌获取
- `PingcodeTokenLogService` - 令牌日志持久化
- `PingcodeProperties` - 配置属性绑定（`pingcode.*` 前缀）

### 配置说明

服务运行在端口 `18700`。PingCode 凭证通过以下配置项设置：
- `pingcode.token.client-id`
- `pingcode.token.client-secret`
