# LLM配置优化总结

## 优化背景

在原有实现中，LLM相关的配置分散在两个不同的配置类中：
1. `LlmApiConfig` - 处理与LLM聊天相关的配置
2. `LlmManageConfig` - 处理与LLM后端管理相关的配置

这种分散的配置存在以下问题：
1. 配置重复（基础URL、超时等参数重复定义）
2. 管理复杂（修改基础参数需要修改多处）
3. 潜在的不一致风险（两套配置可能产生差异）

## 优化措施

1. **合并配置类**：
   - 将`LlmManageConfig`的所有配置项合并到`LlmApiConfig`中
   - 删除`LlmManageConfig`类，统一使用`LlmApiConfig`

2. **重构`LlmApiConfig`**：
   - 添加管理功能相关的属性（向量路径、精确查询路径、模板路径等）
   - 添加新的便捷方法获取完整URL
   - 保持向后兼容性，确保现有代码不受影响

3. **更新`application.properties`**：
   - 删除`llm.manage.*`相关配置
   - 所有配置项统一使用`llm.api.*`前缀

4. **修复相关服务实现**：
   - 更新`LlmManageServiceImpl`，使用`LlmApiConfig`
   - 修复OkHttp的RequestBody.create方法参数顺序问题
   - 优化`LlmServiceImpl`，使用统一的方法获取客户端和URL

## 主要修改点

1. **`LlmApiConfig`类**：
   - 新增属性：`apiVersion`, `vectorPath`, `precisePath`, `templatePath`
   - 新增方法：`getBaseApiUrl()`, `getVectorUrl()`, `getPreciseUrl()`, `getTemplateUrl()`, `getAskUrl()`, `getStreamUrl()`

2. **`LlmManageServiceImpl`类**：
   - 替换`LlmManageConfig`为`LlmApiConfig`
   - 修复`RequestBody.create`方法参数顺序问题
   - 优化文件上传代码，提高可读性

3. **`LlmServiceImpl`类**：
   - 将硬编码的超时参数替换为配置属性
   - 使用`getHttpClient()`方法替代静态客户端实例
   - 使用新的URL获取方法替代字符串拼接

4. **`application.properties`**：
   - 所有LLM相关配置统一使用`llm.api.*`前缀
   - 删除重复的配置项

## 优化效果

1. **配置管理更简单**：
   - 所有LLM相关配置集中在一处
   - 修改基础参数只需要修改一次

2. **代码更清晰**：
   - 统一了获取URL的方式
   - 去除了重复的配置和方法

3. **性能更稳定**：
   - 使用统一的HTTP客户端配置
   - 参数顺序正确，避免潜在问题

## 注意事项

1. **使用正确的API路径**：
   - 确保askPath和streamPath包含完整的路径（包括/api/v1）
   - 确保vectorPath、precisePath、templatePath只包含最后部分（不含/api/v1）

2. **RequestBody创建**：
   - 始终使用`RequestBody.create(MediaType, String)`的参数顺序
   - OkHttp 3.x和OkHttp 4.x的API有差异，注意兼容性

3. **服务注入**：
   - 所有需要LLM配置的服务应注入`LlmApiConfig`，不要使用`LlmManageConfig`

## 后续优化建议

1. **统一HTTP客户端管理**：
   - 考虑创建统一的HTTP客户端工厂类
   - 减少重复创建HTTP客户端实例

2. **错误处理增强**：
   - 增加更详细的错误日志
   - 考虑实现重试机制

3. **可配置的路径处理**：
   - 增加配置验证，确保URL格式正确
   - 自动处理URL中的重复斜杠问题 