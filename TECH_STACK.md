# 🔧 项目技术栈配置 (已优化兼容性)

## ✅ 核心框架 (已确认兼容)

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | LTS版本 |
| Spring Boot | 3.1.1 | ✅ **推荐使用,稳定且兼容性好** |
| Spring Cloud | 2023.0.0 | 最新版本 |
| MyBatis Plus | 3.5.8 | ✅ 已升级,完美兼容Spring Boot 3.1.x |

> **重要**: Spring Boot 3.2.x存在`factoryBeanObjectType`兼容性问题,建议使用3.1.1

---

## ✅ 数据库相关 (已优化)

| 技术 | 版本 | 状态 | 说明 |
|------|------|------|------|
| MySQL Connector | 8.0.33 | ✅ 使用中 | 稳定版本 |
| HikariCP | (Spring Boot内置) | ✅ 使用中 | **Spring Boot默认连接池,性能最佳** |
| Druid | 1.2.21 | ⚠️ 可选 | 如需监控功能可启用 |

**推荐配置**:
- ✅ 使用HikariCP作为连接池(已配置)
- ✅ Druid可选,仅在需要监控时启用

---

## ✅ 工具类库

| 技术 | 版本 | 说明 |
|------|------|------|
| Lombok | 1.18.30 | 最新稳定版 |
| Hutool | 5.8.25 | 国产工具库 |
| Fastjson2 | 2.0.45 | 阿里JSON库 |
| JWT | 0.12.5 | 最新版本 |

---

## ✅ API文档

| 技术 | 版本 | 说明 |
|------|------|------|
| Knife4j | 4.5.0 | ✅ 增强版Swagger,已启用 |
| Springdoc OpenAPI | 2.3.0 | OpenAPI 3.0支持(Knife4j依赖) |

**访问地址**: http://localhost:8081/doc.html

---

## 🔜 待使用技术 (后续服务)

### MinIO (文件存储)
```yaml
推荐版本: 8.5.7+
用途: 文件上传(头像、课件、附件)
服务: resource-service, course-service
```

### RabbitMQ (消息队列)
```yaml
推荐版本: 3.12+
用途: 异步任务处理
服务: course-service, report-service
```

### Redis
```yaml
版本: 7.0+
用途: 缓存、Session
状态: 已配置
```

---

## 📋 版本选择原则

### 1. Spring Boot 3.x 兼容性
- ✅ 优先选择明确支持Spring Boot 3.x的版本
- ✅ 查看官方文档确认兼容性
- ❌ 避免使用仅支持Spring Boot 2.x的库

### 2. 长期支持(LTS)
- ✅ Java 17 (LTS)
- ✅ Spring Boot 3.2.x (稳定版)
- ✅ MySQL 8.0.x (稳定版)

### 3. 社区活跃度
- ✅ 选择维护活跃的项目
- ✅ 有完善文档和社区支持
- ✅ 定期更新修复bug

---

## ⚠️ 已知兼容性问题

### 1. Spring Boot 3.2.x兼容性问题 ⚠️
**问题**: `Invalid value type for attribute 'factoryBeanObjectType': java.lang.String`
**影响**: 导致应用无法启动
**原因**: Spring Boot 3.2.x对FactoryBean元数据处理的改进与部分第三方库不兼容
**解决**: ✅ 使用Spring Boot 3.1.1替代

### 2. MyBatis Plus版本要求
**问题**: MyBatis Plus 3.5.5与Spring Boot 3.x不完全兼容
**解决**: ✅ 升级到3.5.8+

### 3. Druid监控(可选)
**状态**: 当前使用HikariCP,Druid已禁用
**说明**: Druid在Spring Boot 3.x下可能有兼容性问题,HikariCP性能更好

---

## 🎯 后续服务技术栈建议

### resource-service (资源服务)
```yaml
连接池: HikariCP
文件存储: MinIO 8.5.7+
搜索: (可选) Elasticsearch 8.x
```

### course-service (课程服务)
```yaml
连接池: HikariCP
消息队列: RabbitMQ 3.12+
缓存: Redis 7.0+
```

### community-service (社区服务)
```yaml
连接池: HikariCP
实时通信: (可选) WebSocket
```

### report-service (报告服务)
```yaml
连接池: HikariCP
消息队列: RabbitMQ 3.12+
数据分析: (可选) 自定义算法
```

---

## 📝 配置文件模板

### HikariCP配置 (推荐)
```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/database
    username: root
    password: password
    
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      connection-test-query: SELECT 1
```

### MyBatis Plus配置
```yaml
mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: com.edu.platform.*.entity
  configuration:
    map-underscore-to-camel-case: true
    cache-enabled: false
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: isDeleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

---

## ✅ 当前项目状态

- ✅ 核心框架配置完成
- ✅ 连接池优化完成 (HikariCP)
- ✅ MyBatis Plus版本升级
- ✅ 编译测试通过
- ⏳ 等待启动验证

---

**更新时间**: 2026-01-18  
**项目**: 课程思政融合育人平台  
**原则**: 优先使用兼容性好、性能优的最新稳定版本
