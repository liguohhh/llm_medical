# 医疗辅助问答系统 RESTful API 文档

## 基础信息

- 基础URL: `http://localhost:8086`
- 请求格式: JSON
- 响应格式: JSON
- 认证方式: Cookie 认证

## 通用响应格式

所有API响应均采用以下JSON格式:

```json
{
  "code": 200,       // 状态码：200-成功，非200-失败
  "message": "成功",  // 响应消息
  "data": {}         // 响应数据，可能为null
}
```

## 错误码说明

| 错误码 | 说明 |
| ----- | ---- |
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证或认证失败 |
| 403 | 权限不足 |
| 500 | 服务器内部错误 |

## 1. 用户认证接口

### 1.1 用户注册

- **URL**: `/api/auth/register`
- **方法**: POST
- **描述**: 注册新用户（默认为病人角色）
- **请求参数**:

```json
{
  "username": "patient1",    // 账户名，必填，4-20个字符
  "password": "123456",      // 密码，必填，6-20个字符
  "realName": "张三",        // 姓名，必填
  "gender": 1,               // 性别：0-女，1-男，必填
  "age": 30                  // 年龄，必填
}
```

- **成功响应**:

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 1,
    "username": "patient1",
    "realName": "张三",
    "gender": 1,
    "age": 30,
    "userType": 0,
    "directionId": null,
    "direction": null,
    "createTime": "2023-10-01 12:00:00"
  }
}
```

- **失败响应**:

```json
{
  "code": 400,
  "message": "用户名已存在",
  "data": null
}
```

### 1.2 用户登录

- **URL**: `/api/auth/login`
- **方法**: POST
- **描述**: 用户登录，成功后会设置Cookie
- **请求参数**:

```json
{
  "username": "patient1",   // 账户名，必填
  "password": "123456"      // 密码，必填
}
```

- **成功响应**:

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 1,
    "username": "patient1",
    "realName": "张三",
    "gender": 1,
    "age": 30,
    "userType": 0,
    "directionId": null,
    "direction": null,
    "createTime": "2023-10-01 12:00:00"
  }
}
```

- **失败响应**:

```json
{
  "code": 401,
  "message": "用户名或密码错误",
  "data": null
}
```

### 1.3 获取当前用户信息

- **URL**: `/api/auth/info`
- **方法**: GET
- **描述**: 获取当前登录用户的信息
- **请求参数**: 无
- **成功响应**:

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 1,
    "username": "patient1",
    "realName": "张三",
    "gender": 1,
    "age": 30,
    "userType": 0,
    "directionId": null,
    "direction": null,
    "createTime": "2023-10-01 12:00:00"
  }
}
```

- **失败响应**:

```json
{
  "code": 401,
  "message": "未登录",
  "data": null
}
```

### 1.4 用户注销

- **URL**: `/api/auth/logout`
- **方法**: POST
- **描述**: 用户注销登录，清除Cookie
- **请求参数**: 无
- **成功响应**:

```json
{
  "code": 200,
  "message": "成功",
  "data": null
}
```

## 2. 安全说明

### 2.1 认证机制

系统使用Spring Security实现基于Cookie的会话认证:

1. 用户登录成功后，服务器创建会话并返回会话ID，保存在Cookie中
2. 浏览器后续请求会自动发送Cookie，服务器验证会话ID
3. 注销时，服务器销毁会话，并清除Cookie

### 2.2 权限控制

系统基于角色的权限控制，主要有三种角色:

1. **ROLE_PATIENT**: 病人，可以与智能体交互，查看自己的处方等
2. **ROLE_DOCTOR**: 医生，可以审核处方
3. **ROLE_ADMIN**: 管理员，可以管理用户、智能体等

不同角色可以访问不同的API接口，未经授权的访问将返回403状态码。

### 2.3 密码安全

- 密码采用BCrypt算法加密存储
- 密码传输过程中建议使用HTTPS加密传输 