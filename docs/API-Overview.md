# 教育平台 API 文档总览

> **版本**：v1.0 | **网关地址**：`http://localhost:8080`  
> 所有接口均通过 `platform-gateway` 统一路由，前端只需使用网关端口。

---

## 一、通用约定

### 认证方式

所有需要登录的接口，请在 HTTP Header 中携带：

```
Authorization: Bearer <JWT Token>
```

- Token 在登录接口 `/api/v1/auth/login` 返回。
- 网关会自动解析 Token，向下游服务注入 `X-User-Id`、`X-Username`、`X-User-Roles` 三个 Header，下游服务从中读取用户信息，**前端无需手动传递这三个 Header**。

### 统一响应格式

```json
{
  "code": 200,          // 200=成功，非200=失败
  "message": "操作成功",
  "data": {}            // 具体业务数据，失败时可能为 null
}
```

- **成功**：`code == 200`
- **未认证**：`code == 401`
- **无权限**：`code == 403`
- **业务错误**：`code == 500` 或其他自定义码

### 分页响应格式

```json
{
  "code": 200,
  "data": {
    "total": 100,
    "pageNum": 1,
    "pageSize": 10,
    "records": []
  }
}
```

### 通用注意事项

| 注意点 | 说明 |
|--------|------|
| 路由前缀 | 所有接口路径已包含 `/api/v1`，网关直接透传 |
| Content-Type | POST/PUT 请求体使用 `application/json` |
| 文件上传 | 使用 `multipart/form-data` |
| ID 类型 | 所有 ID 为 Long 类型，JS 环境下注意大数精度，建议以字符串接收 |
| 时间格式 | 统一使用 ISO 8601：`yyyy-MM-dd HH:mm:ss` |
| 默认分页 | pageNum 默认 1，pageSize 默认 10 |

---

## 二、角色说明

| 角色 | 说明 |
|------|------|
| `ADMIN` | 系统管理员，权限最高 |
| `SCHOOL_LEADER` | 校领导，可查看报表和审核 |
| `TEACHER` | 教师 |
| `ASSISTANT` | 助教 |
| `STUDENT` | 学生 |

---

## 三、模块文档索引

| 模块 | 文件 | 主要功能 |
|------|------|----------|
| 用户认证 & 管理 | [API-01-UserService.md](./API-01-UserService.md) | 注册、登录、用户管理、学校、文件上传 |
| 课程管理 | [API-02-CourseService.md](./API-02-CourseService.md) | 课程、章节、课件、任务、成员、考试、公告 |
| 社区功能 | [API-03-CommunityService.md](./API-03-CommunityService.md) | 讨论话题、评论、小组、协作文档、点赞 |
| 资源管理 | [API-04-ResourceService.md](./API-04-ResourceService.md) | 资源库、分类、标签、文件上传、审核 |
| 审核中心 | [API-05-AuditService.md](./API-05-AuditService.md) | 统一内容审核（课件/帖子/评论） |
| 报告 & 画像 | [API-06-ReportService.md](./API-06-ReportService.md) | 素养画像、成长轨迹、报告生成下载、行为埋点 |
