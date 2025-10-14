# unione

<div align="center">
  <img src="https://img.shields.io/badge/license-Apache%202.0-blue" alt="License" />
  <img src="https://img.shields.io/badge/Spring%20Boot-3.4.0-green" alt="Spring Boot" />
  <img src="https://img.shields.io/badge/Java-21-orange" alt="Java" />
  <img src="https://img.shields.io/badge/Spring%20Cloud-4.2.0-blue" alt="Spring Cloud" />
</div>

## 项目简介
unione cloud 是一个企业级统一低代码开发平台，旨在为企业提供一站式低代码建站服务。通过可视化拖拽、配置等方式，帮助企业快速构建各类业务应用，大幅降低开发成本和技术门槛。

## 功能特性
- **可视化开发**：拖拽式界面设计，所见即所得
- **组件化架构**：丰富的功能组件库，支持自定义扩展
- **多模块设计**：基于微服务架构，各模块松耦合高内聚
- **安全认证**：完善的用户认证与权限管理体系
- **API管理**：统一的接口管理与文档自动化生成
- **数据管理**：灵活的数据模型设计与管理
- **工作流支持**：可视化工作流设计与运行

## 技术栈
- **核心框架**：Spring Boot 3.4.0、Spring Cloud 4.2.0
- **开发语言**：Java 21
- **数据库**：MySQL 8.0.21
- **缓存**：Redis、JetCache
- **接口文档**：Swagger 3.0.0、Knife4j 4.5.0
- **服务注册发现**：Nacos 2023.0.3.2
- **负载均衡**：Netflix Feign
- **代码生成**：自定义代码生成模块
- **安全框架**：Spring Security、JWT
- **工具库**：Hutool、Lombok、Jackson

## 项目结构
unione 采用模块化架构设计，各模块职责明确，便于维护和扩展：

```
unione/
├── unione-core/         # 核心功能模块，提供基础服务和通用组件
├── unione-beetsql/      # 数据库访问模块，基于BeetSQL框架
├── unione-web/          # Web服务基础模块，业务服务扩展组件
├── unione-gateway/      # API网关模块，统一入口和路由管理
├── unione-job/          # 定时任务模块，管理后台定时任务
├── unione-portal/       # 门户模块，提供用户界面和交互入口
├── unione-util/         # 工具类模块，提供通用工具方法
├── unione-starter/      # 启动模块，整合各组件并提供启动入口
└── unione-codegen/      # 代码生成模块，自动生成基础代码
```

## 安装教程
### 前提条件
- JDK 21 或更高版本
- Maven 3.6+ 构建工具
- MySQL 8.0+ 数据库
- Redis 5.0+ 缓存服务
- Nacos 2023.0.3.2 服务注册中心

### 步骤说明
1. **克隆代码仓库 github**
   ```bash
   git clone https://github.com/unione-cloud/unione.git
   cd unione
   ```
1.1. **克隆代码仓库 gitee**
   ```bash
   git clone https://gitee.com/unione-cloud/unione.git
   cd unione
   ```

2. **配置数据库**
   - 创建数据库：`CREATE DATABASE unione DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`
   - 配置数据库连接信息（在各模块的配置文件中）

3. **配置Nacos**
   - 启动Nacos服务
   - 配置服务注册信息

4. **构建项目**
   ```bash
   mvn clean install
   ```

5. **启动服务**
   ```bash
   # 依次启动各模块
   cd unione-starter
   mvn spring-boot:run
   ```

## 使用说明
1. **访问管理后台**
   - 浏览器访问：`http://localhost:8080/portal`
   - 默认账号：admin
   - 默认密码：admin123

2. **创建应用**
   - 登录管理后台
   - 点击"新建应用"按钮
   - 填写应用基本信息
   - 选择应用模板
   - 点击"创建"完成

3. **设计应用界面**
   - 进入应用设计器
   - 从左侧组件库拖拽组件到画布
   - 配置组件属性和数据绑定
   - 保存并发布应用

4. **API接口调用**
   - 访问Swagger文档：`http://localhost:8080/portal/doc.html`
   - 根据接口文档调用相应API
   - 注意：所有接口需要携带有效的JWT令牌

## 贡献指南
我们非常欢迎社区贡献！如果您想参与项目开发，请按照以下步骤进行：

1. **Fork 代码仓库**
   在Gitee上Fork项目到您自己的账号下

2. **创建分支**
   ```bash
   git checkout -b Feat_xxx
   ```

3. **提交代码**
   ```bash
   git add .
   git commit -m "feat: 添加xxx功能"
   ```

4. **推送代码**
   ```bash
   git push origin Feat_xxx
   ```

5. **创建Pull Request**
   在Gitee上提交Pull Request，描述您的改动内容和目的

## 许可证
本项目采用 Apache License 2.0 许可证开源，详情请查看 [LICENSE](LICENSE) 文件。

## 联系方式
- **项目负责人**：Jeking Yang
- **邮箱**：jeking217@163.com
- **项目地址github**：https://github.com/unione-cloud/unione
- **项目地址gitee**：https://gitee.com/unione-cloud/unione


