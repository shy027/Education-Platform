# 环境配置说明

## 阿里云OSS配置

### 方式1: 使用环境变量 (推荐)

在系统环境变量或IDE运行配置中设置:

```bash
# Windows (PowerShell)
$env:ALIYUN_OSS_ACCESS_KEY_ID="your-access-key-id"
$env:ALIYUN_OSS_ACCESS_KEY_SECRET="your-access-key-secret"

# Linux/Mac
export ALIYUN_OSS_ACCESS_KEY_ID="your-access-key-id"
export ALIYUN_OSS_ACCESS_KEY_SECRET="your-access-key-secret"
```

### 方式2: 使用本地配置文件

1. 复制 `application-local.yml.example` 为 `application-local.yml`
2. 填写真实的密钥信息
3. 启动时指定profile: `--spring.profiles.active=local`

**注意**: `application-local.yml` 已在 `.gitignore` 中,不会被提交到Git

### 方式3: IDEA运行配置

1. 打开 Run/Debug Configurations
2. 在 Environment variables 中添加:
   ```
   ALIYUN_OSS_ACCESS_KEY_ID=your-access-key-id;ALIYUN_OSS_ACCESS_KEY_SECRET=your-access-key-secret
   ```

## 其他敏感配置

### 数据库密码
```yaml
spring:
  datasource:
    password: ${DB_PASSWORD:123456}
```

### Redis密码
```yaml
spring:
  data:
    redis:
      password: ${REDIS_PASSWORD:}
```

## 生产环境配置

生产环境建议使用配置中心(如Nacos、Apollo)或云服务商的密钥管理服务。

**切勿将真实密钥提交到Git仓库!**
