# 医疗辅助问答系统

基于Spring Boot和Vue的医疗辅助问答系统，集成大模型提供智能诊断和处方服务。

## 系统架构

- 前端：Vue
- 后端：Spring Boot 3.4.5
- 大模型层：Python FastAPI
- 数据库：MySQL

## 功能特点

- 用户注册登录（病人、医生、管理员）
- 病人与智能体对话
- 智能体生成处方
- 医生审核处方
- 管理员管理用户和智能体

## 环境要求

- JDK 21
- Maven 3.8+
- MySQL 8.0+
- Python 3.8+

## 配置说明

### 数据库配置

默认数据库配置如下（在`application.properties`中）：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/llm_medical?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&useSSL=false
spring.datasource.username=root
spring.datasource.password=your_password
```

请根据实际环境修改数据库连接信息。

### 运行后端服务

1. 克隆代码库：
   ```bash
   git clone [仓库地址]
   ```

2. 进入后端目录：
   ```bash
   cd server
   ```

3. 编译打包：
   ```bash
   mvn clean package
   ```

4. 运行：
   ```bash
   java -jar target/server-0.0.1-SNAPSHOT.jar
   ```

服务将在默认端口8086启动。

## API测试

### 使用Python测试脚本

项目提供了Python测试脚本`test_auth_api.py`，用于测试用户认证API：

1. 安装依赖：
   ```bash
   pip install requests
   ```

2. 运行测试脚本：
   ```bash
   python test_auth_api.py
   ```

测试脚本将执行以下测试流程：
- 注册新用户
- 尝试使用错误密码登录
- 使用正确密码登录
- 获取当前用户信息
- 注销登录
- 注销后尝试获取用户信息

### 使用API文档测试

详细的API文档请参考`API文档.md`文件，其中包含了所有API端点的详细说明和示例。

## 安全说明

- 密码采用BCrypt算法加密存储
- 使用Spring Security实现认证和授权
- 基于Cookie的会话认证机制
- 角色基础的权限控制

## 开发者文档

请参考`server/docs/设计实现说明.md`文件，其中包含了系统架构设计和实现细节。