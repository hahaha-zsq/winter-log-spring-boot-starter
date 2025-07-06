# 🌟 winter-log-spring-boot-starter

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Java support](https://img.shields.io/badge/Java-1.8+-green.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.6+-blue.svg)](https://spring.io/projects/spring-boot)

## 📖 项目简介

winter-log-spring-boot-starter 是一个轻量级、易用的日志记录组件，通过AOP技术自动捕获系统操作日志和异常日志，帮助开发者更好地监控和分析应用程序的运行情况。该组件可以无缝集成到Spring Boot项目中，极大地简化了日志记录的开发工作。

## ✨ 特性

- 🔍 基于AOP实现，自动捕获使用 `@SystemLog` 注解的方法调用
- 🚨 自动记录系统异常信息，包含详细的堆栈信息
- 📊 完整记录HTTP请求信息（参数、请求体、响应体、请求头等）
- 🔧 支持自定义日志处理实现，可轻松集成数据库或消息队列
- 🌐 自动获取客户端IP、地理位置、浏览器信息等
- 🔒 自动过滤敏感信息（如密码字段）
- ⏱️ 可配置仅记录超过指定执行时间的方法
- 📦 支持文件上传等特殊请求类型

## 🔨 环境要求

- JDK 11+
- Spring Boot 2.6+

## 🚀 快速开始

### Maven依赖

```xml
<dependency>
    <groupId>io.github.hahaha-zsq</groupId>
    <artifactId>winter-log-spring-boot-starter</artifactId>
    <version>0.0.1</version>
</dependency>
```

### 启用日志功能

在启动类上添加 `@EnableWinterLog` 注解：

```java
import com.zsq.winter.log.annotation.EnableWinterLog;

@SpringBootApplication
@EnableWinterLog
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

### 配置参数

在 `application.yml` 中添加配置：

```yaml
winter-log:
  # 记录运行时间超过此值的方法（单位：毫秒，默认为0，表示记录所有方法）
  run-time: 0
  # 响应结果最大记录长度（默认1000字符）
  result-length: 1000
  # 是否打印banner（默认true）
  is-print: true
```

### 使用注解记录日志

在需要记录的方法上添加 `@SystemLog` 注解：

```java
import com.zsq.winter.log.annotation.SystemLog;
import com.zsq.winter.log.entity.OperationLogType;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @SystemLog(
        operationModule = "用户管理", 
        operationType = OperationLogType.ADD, 
        operationDesc = "新增用户"
    )
    @PostMapping
    public Result<?> addUser(@RequestBody UserDTO userDTO) {
        // 业务代码
        return Result.ok();
    }
}
```

## 📋 核心功能

### 自定义日志处理

默认情况下，日志会通过 `DefaultLogServiceImpl` 输出到控制台。如需自定义日志处理方式（如保存到数据库），可以实现 `LogService` 接口，并注入到Spring容器中：

```java
import com.zsq.winter.log.entity.ErrorLog;
import com.zsq.winter.log.entity.OperationLog;
import com.zsq.winter.log.service.LogService;
import org.springframework.stereotype.Service;

@Service
public class CustomLogServiceImpl implements LogService {
    @Override
    public void logHandler(OperationLog operationLog) {
        // 💾 自定义操作日志处理逻辑
        System.out.println(">>>自定义日志实现，内容是：" + operationLog);
        
        // 可以将日志保存到数据库、发送到消息队列等
    }

    @Override
    public void errLogHandler(ErrorLog errorLog) {
        // ❌ 自定义错误日志处理逻辑
        System.out.println(">>>自定义错误日志实现，内容是：" + errorLog);
    }
}
```

## 📋 日志数据结构

### 操作日志 (OperationLog)

| 字段 | 说明 |
|------|------|
| 🆔 id | 日志ID |
| 🔑 uuid | 请求唯一标识 |
| 📝 operationType | 操作类型 |
| 📄 operationDesc | 操作描述 |
| 📦 operationModule | 操作模块 |
| 🌐 browserInfo | 浏览器信息 |
| 🔗 url | 请求URL |
| 📨 requestHeader | 请求头 |
| 🛠️ method | 请求方法 |
| 🧩 interfaceFullPath | 接口全路径 |
| 🖥️ ip | 客户端IP |
| 📍 address | IP地址所在地 |
| 📥 requestParamArgs | 请求参数 |
| 📤 requestBodyArgs | 请求体 |
| ⏱️ requestTime | 请求时间 |
| ⌛ responseTime | 响应时间 |
| ⏲️ spendTime | 耗时(毫秒) |
| 📊 response | 响应内容 |

### 错误日志 (ErrorLog)

| 字段 | 说明 |
|------|------|
| 🆔 id | 日志ID |
| 📥 errRequestParam | 错误请求参数 |
| 🏷️ errName | 错误名称 |
| 📝 errMessage | 错误信息 |
| 🛠️ operationMethod | 操作方法 |
| 🔗 operationUrl | 操作URL |
| 🖥️ operationIp | 操作IP |
| 📍 address | IP地址所在地 |
| 🕒 createTime | 创建时间 |
## 📊 操作类型常量
系统预定义了以下操作类型常量（ OperationLogType ）：

- 🔍 QUERY - 查询
- ➕ ADD - 新增
- 🔄 MODIFY - 修改
- ✅ VALIDATOR - 校验
- 📤 EXPORT - 导出
- ↩️ WITHDRAW - 撤回
- 📨 SEND - 发送
- 📤 UPLOAD - 上传
- 📥 IMPORT - 导入
- 📥 DOWNLOAD - 下载
- ❌ DELETE - 删除
- 🔄 JOB - 任务

## 🎯 使用示例
```java
@SystemLog(operationModule = "用户管理", operationType = OperationLogType.ADD, operationDesc = "新增用户")
@PostMapping("/user")
public Result<?> addUser(@RequestBody UserDTO userDTO) {
    // ... 业务代码 ...
    return Result.ok();
}
```
输出示例
```plaintext    
>>>自定义日志实现，内容是：OperationLog(
  id=null, 
  uuid=81053121faad44c986acf9819da6582a, 
  operationType=发送, 
  operationDesc=测试rocketmq, 
  operationModule=会员, 
  browserInfo={"browserType":"Unknown","platform":"Unknown","mobile":0,"os":"Unknown","engine":"Unknown"}, 
  url=http://localhost:6446/enhance/member, 
  requestHeader={"content-length":"96","host":"localhost:6446","content-type":"application/json",...}, 
  method=POST, 
  interfaceFullPath=com.zsq.demo.rocketmq.EnhanceProduceController.member, 
  ip=0:0:0:0:0:0:0:1, 
  address=内网IP, 
  requestParamArgs=message:MemberMessage(userName=大胆刁民,age=17,money=13.343,birthday=2000-01-01), 
  requestBodyArgs={"userName":"大胆刁民","age":17,"money":13.343,"birthday":"2000-01-01"}, 
  requestTime=2024-05-21 16:33:49, 
  responseTime=2024-05-21 16:33:50, 
  spendTime=89, 
  response={"code":200,"message":"","data":{"sendStatus":"SEND_OK",...}}
)
```
## 🔍 项目结构

```
winter-log-spring-boot-starter/
├── src/main/java/com/zsq/winter/log/
│   ├── annotation/           # 注解定义
│   ├── aspect/               # AOP切面
│   ├── config/               # 配置类
│   ├── entity/               # 实体类
│   ├── filter/               # 请求过滤器
│   ├── service/              # 日志服务接口及实现
│   └── util/                 # 工具类
└── src/main/resources/
    └── META-INF/             # Spring Boot自动配置
```

## 🔔 注意事项

- 必须在启动类上添加 `@EnableWinterLog` 注解才能启用日志功能
- 自定义日志处理类必须注入到Spring容器中，否则将使用默认实现
- 可通过 `run-time` 参数控制只记录执行时间超过阈值的方法
- 密码等敏感信息（包含 `password` 关键字）会被自动过滤（显示为 `***`）
- 支持文件上传等特殊请求类型的日志记录

## 🤝 贡献指南

欢迎提交问题和功能需求！如果您想为项目做出贡献，请遵循以下步骤：

1. Fork 本仓库
2. 创建您的特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交您的更改 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 打开一个 Pull Request

## 📄 许可证

本项目采用 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) 许可证。

## 📚 更多信息

- 作者：dadandiaoming
- 邮箱：2595915122@qq.com
- 版本：v0.0.1

> ❤️ 欢迎 Star & PR，更多特性敬请期待！