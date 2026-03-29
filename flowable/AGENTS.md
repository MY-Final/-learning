# AGENTS.md

本文件用于指导在本仓库中工作的智能编码代理（Agent）。

## 仓库结构

- 仓库根目录下只有一个 Maven 模块：`flowableDemo/`。
- Java 主代码目录：`flowableDemo/src/main/java`。
- 测试代码目录：`flowableDemo/src/test/java`。
- 应用配置文件：`flowableDemo/src/main/resources/application.yml`。
- BPMN 流程文件目录：`flowableDemo/src/main/resources/process/`。
- Maven 构建文件：`flowableDemo/pom.xml`。

## Agent 工作约定

1. 默认从仓库根目录执行命令，除非有特别说明。
2. 优先使用 Maven Wrapper（`flowableDemo/mvnw` 或 `flowableDemo/mvnw.cmd`），不要依赖系统 Maven。
3. 变更应尽量小而精准，只覆盖用户请求范围。
4. 不要编辑 `target/` 下的构建产物。
5. 除非用户明确要求，不要提交 `.idea/` 等 IDE 本地配置。
6. 修改前先理解现有代码风格，新增代码需保持一致。
7. 涉及数据库或流程定义变更时，优先保证可回滚和可复现。

## 构建 / 运行 / 测试命令

### 在仓库根目录执行（`C:\Users\34861\Desktop\learning\flowable`）

- 编译：
  - `flowableDemo/mvnw -f flowableDemo/pom.xml clean compile`
- 运行全部测试：
  - `flowableDemo/mvnw -f flowableDemo/pom.xml test`
- 打包（生成 JAR）：
  - `flowableDemo/mvnw -f flowableDemo/pom.xml clean package`
- 启动 Spring Boot：
  - `flowableDemo/mvnw -f flowableDemo/pom.xml spring-boot:run`

### 在模块目录执行（`flowableDemo/`）

- 编译：`./mvnw clean compile`（Windows：`mvnw.cmd clean compile`）
- 测试：`./mvnw test`（Windows：`mvnw.cmd test`）
- 打包：`./mvnw clean package`（Windows：`mvnw.cmd clean package`）
- 运行：`./mvnw spring-boot:run`（Windows：`mvnw.cmd spring-boot:run`）

## 单测执行（重点）

本项目基于 Spring Boot 父 POM，默认使用 Maven Surefire 执行测试。

- 运行单个测试类：
  - `flowableDemo/mvnw -f flowableDemo/pom.xml -Dtest=FlowableDemoApplicationTests test`
- 运行单个测试方法：
  - `flowableDemo/mvnw -f flowableDemo/pom.xml -Dtest=FlowableDemoApplicationTests#startFlow test`
- 运行同类下多个方法：
  - `flowableDemo/mvnw -f flowableDemo/pom.xml -Dtest=FlowableDemoApplicationTests#startFlow+findFlow test`
- 按名称模式运行：
  - `flowableDemo/mvnw -f flowableDemo/pom.xml -Dtest=*ApplicationTests* test`

若出现 “No tests were executed”，优先检查：

- 测试类名是否以 `Test` 或 `Tests` 结尾。
- 测试方法是否使用 `@Test` 注解。
- 测试类/方法可见性与命名是否符合 Surefire 识别规则。

## Lint / 格式化现状

- `pom.xml` 未配置 Checkstyle / SpotBugs / PMD。
- `pom.xml` 未配置统一格式化插件（如 spotless/fmt）。
- 因此默认采用 IDE Java 格式化规则，并与邻近代码保持一致。
- 提交前务必清理无用 import，禁止通配符 import。

## 依赖与平台信息

- Java 版本：`17`（`pom.xml` 中 `java.version`）。
- Spring Boot Parent：`3.5.13`。
- Flowable 版本：`7.2.0`。
- 数据库驱动：MySQL（`com.mysql:mysql-connector-j`）。
- 使用了 Lombok，并在 `maven-compiler-plugin` 中配置了注解处理。

## 配置与运行环境说明

- 应用默认连接 `application.yml` 中配置的 MySQL。
- 默认服务端口：`9070`。
- Flowable 自动建表/升级已开启：`flowable.database-schema-update: true`。
- `org.flowable` 与 `com.myfinal` 日志级别为 `DEBUG`。
- `@SpringBootTest` 测试通常依赖可用数据库，执行前确认本地环境可连通。

## 安全与敏感信息

- 当前仓库配置/测试中存在明文数据库凭据。
- 不要新增任何硬编码密钥、密码、令牌。
- 新增配置优先使用环境变量或 profile 覆盖。
- 除非用户明确要求，不要修改现有凭据行为。

## 代码风格规范（Java）

### Imports

- import 分组顺序建议：
  1. `java.*` / `javax.*`
  2. 第三方依赖（`org.*`、`com.*` 等）
  3. 项目内包
- 分组之间空一行。
- 禁止 `*` 通配符导入。
- 删除未使用 import。

### 格式化

- 使用 4 空格缩进，不使用 Tab。
- 行宽建议不超过 120 字符。
- 每个文件仅保留一个顶级 `public` 类。
- `if/else/for/while` 必须使用花括号。
- 方法、字段、逻辑块之间保持适度空行。

### 类型与 API 使用

- 声明优先接口类型（如 `List`、`Map`），实例化用具体实现。
- 显式使用泛型，避免 raw type。
- 不会变化的局部变量优先加 `final`。
- 方法职责单一，复杂逻辑及时拆分私有方法。
- 生产代码优先构造器注入；测试中可接受字段注入。

### 命名规范

- 类/接口：`UpperCamelCase`。
- 方法/字段/局部变量：`lowerCamelCase`。
- 常量：`UPPER_SNAKE_CASE`。
- 包名：全小写，点分层级。
- 测试类名应准确对应被测行为。

说明：仓库内已有 `myBean`、`myListener01` 等历史命名；新增代码请优先遵循标准 Java 命名。

### 异常处理与日志

- 禁止静默吞异常。
- 在服务边界可包装底层异常并补充上下文。
- 非演示逻辑优先使用 `slf4j`，避免 `System.out.println`。
- 日志和异常信息应包含关键上下文（如流程定义 ID、任务 ID、业务键）。
- 对非法输入尽早失败（空值、空字符串、格式校验）。

### Spring / Flowable 约定

- Spring 组件建议放在 `com.myfinal.flow` 包路径下，确保扫描生效。
- 业务逻辑优先 `@Service`，通用组件用 `@Component`。
- Flowable 资源名和部署名尽量稳定，避免迁移歧义。
- 可复用测试中避免硬编码运行时 ID，优先查询得到。

## 测试实践建议

- 默认测试框架为 JUnit 5（`org.junit.jupiter.api.Test`）。
- 先跑单类/单方法验证改动，再跑全量 `test`。
- 新增测试数据应尽量可重复、可隔离。
- 优先写断言，不建议仅靠控制台打印判断通过。
- 涉及流程审批链路时，明确前置部署与变量准备步骤。

## 不应编辑的文件与产物

- `flowableDemo/target/**`（构建产物）
- `.idea/**`（本地 IDE 配置）
- `replay_pid*.log`（运行日志）

## Cursor / Copilot 规则文件

- 当前仓库未发现 `.cursor/rules/` 目录。
- 当前仓库未发现 `.cursorrules` 文件。
- 当前仓库未发现 `.github/copilot-instructions.md` 文件。
- 若后续新增上述规则文件，应视为更高优先级指令来源。

## 提交前快速检查清单

1. 针对改动运行编译或相关测试。
2. 对核心行为至少执行一次单测命令（单类或单方法）。
3. 清理调试输出、无用 import、临时代码。
4. 确认未引入新的敏感信息。
5. 在说明中写明假设条件（尤其是数据库依赖与流程前置条件）。
