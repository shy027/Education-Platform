# 报告 & 画像服务 API 文档

**服务名**：`report-service`  
**网关路由前缀**：`/api/v1`

---

## 1. 行为埋点（BehaviorController）

**Base Path**：`/api/v1/behaviors`

> 用于记录学生的学习行为数据，供素养画像计算使用。前端应在以下场景自动上报：学生观看课件、参与讨论、提交作业等。

---

### 1.1 记录学习行为

`POST /api/v1/behaviors/log` 🔐

**Request Body**（BehaviorLogRequest）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | ✅ | 用户ID（可从 UserContext 自动获取） |
| courseId | Long | ✅ | 课程ID |
| behaviorType | String | ✅ | 行为类型（见下表） |
| targetId | Long | ❌ | 操作目标ID（课件ID、帖子ID 等） |
| duration | Integer | ❌ | 持续时长（秒） |
| extra | String | ❌ | 扩展信息（JSON 字符串） |

**行为类型（behaviorType）**：

| 值 | 说明 |
|----|------|
| `WATCH_VIDEO` | 观看视频 |
| `READ_DOC` | 阅读文档 |
| `POST_COMMENT` | 发表讨论 |
| `SUBMIT_ANSWER` | 提交作业/考试 |
| `GROUP_DISCUSS` | 小组讨论 |
| `RESOURCE_VIEW` | 查看资源 |

---

## 2. 素养画像（ProfileController）

**Base Path**：`/api/v1/profiles`

> 五维素养画像：理论素养、实践能力、价值认同、创新思维、社会责任感。

---

### 2.1 获取我的素养画像

`GET /api/v1/profiles/my?courseId=1001` 🔐

**Response Data**（ProfileResponse）：

```json
{
  "userId": 200,
  "courseId": 1001,
  "theoreticalLiteracy": 85.5,
  "practicalAbility": 72.0,
  "valueIdentity": 90.0,
  "innovativeThinking": 68.5,
  "socialResponsibility": 80.0,
  "totalScore": 79.2,
  "level": "良好",
  "updatedTime": "2024-01-15 08:00:00"
}
```

---

### 2.2 获取指定用户素养画像（教师/管理员）

`GET /api/v1/profiles/{userId}?courseId=1001` 🔐 `[TEACHER/ADMIN]`

---

### 2.3 获取雷达图数据

`GET /api/v1/profiles/radar?courseId=1001` 🔐

**Response Data**（RadarDataResponse）：

```json
{
  "dimensions": ["理论素养", "实践能力", "价值认同", "创新思维", "社会责任感"],
  "scores": [85.5, 72.0, 90.0, 68.5, 80.0],
  "classAvg": [75.0, 68.0, 82.0, 65.0, 76.0]
}
```

> ⚠️ 配合 ECharts 雷达图使用，`classAvg` 为班级平均。

---

### 2.4 获取成长轨迹

`GET /api/v1/profiles/growth-track?courseId=1001&days=30` 🔐

**Query Params**：

| 参数 | 默认值 | 说明 |
|------|--------|------|
| courseId | 必填 | 课程ID |
| days | 30 | 查询最近 N 天 |

**Response Data**（GrowthTrackResponse）：

```json
{
  "dates": ["2024-01-01", "2024-01-02", ...],
  "totalScores": [70.0, 72.5, 75.0, ...]
}
```

---

### 2.5 获取学习统计

`GET /api/v1/profiles/statistics?courseId=1001&days=30` 🔐

**Response Data**（StatisticsResponse）：

```json
{
  "totalWatchDuration": 3600,
  "totalPosts": 5,
  "totalAnswers": 3,
  "completedCourseware": 8,
  "totalCourseware": 12
}
```

---

### 2.6 手动触发画像计算（管理员调试用）

`POST /api/v1/profiles/calculate?courseId=1001` 🔐 `[ADMIN]`

> 正常情况下画像由后台定时任务自动计算，此接口仅供调试使用。

---

### 2.7 计算单个用户画像

`POST /api/v1/profiles/calculate/user?userId=200&courseId=1001` 🔐

---

## 3. 报告管理（ReportController）

**Base Path**：`/api/v1/reports`

> 报告文件存储在 OSS，通过预签名 URL 下载（有效期 1 小时）。

---

### 3.1 生成课程报告

`POST /api/v1/reports/course/{courseId}/generate` 🔐 `[TEACHER/ADMIN]`

**Response Data**：reportId（Long）

> ⚠️ 报告生成为**异步**过程，调用后立即返回 reportId，实际内容需轮询状态接口确认完成。

---

### 3.2 查询报告状态

`GET /api/v1/reports/{reportId}/status`（无特殊权限）

**Response Data**（ReportStatusResponse）：

```json
{
  "reportId": 7001,
  "status": 1,
  "statusDesc": "生成中",
  "progress": 60,
  "createdTime": "2024-01-15 10:00:00",
  "finishedTime": null
}
```

| status | 说明 |
|--------|------|
| 0 | 等待中 |
| 1 | 生成中 |
| 2 | 已完成 |
| 3 | 生成失败 |

> ⚠️ **前端轮询建议**：每 3 秒轮询一次，状态为 2 或 3 时停止轮询。

---

### 3.3 下载报告（获取预签名 URL）

`GET /api/v1/reports/{reportId}/download`（无特殊权限）

**Response Data**：`"https://oss.xxx.com/reports/xxx.pdf?signature=xxx&expires=xxx"`

> ⚠️ URL 有效期为 **1 小时**，不要缓存，每次下载重新获取。前端直接用此 URL 打开或 `window.open()` 下载。

---

### 3.4 查询课程报告列表

`GET /api/v1/reports/course/{courseId}?pageNum=1&pageSize=10`（无特殊权限）

**Response Data**（`PageResult<ReportDTO>`）：

```json
{
  "records": [{
    "id": 7001,
    "courseId": 1001,
    "courseName": "思政教育实践",
    "reportType": 1,
    "status": 2,
    "generatorId": 100,
    "generatorName": "李老师",
    "createdTime": "2024-01-15 10:00:00",
    "finishedTime": "2024-01-15 10:02:00",
    "downloadCount": 3
  }]
}
```

---

### 3.5 查询所有报告列表（管理员）

`GET /api/v1/reports` 🔐 `[ADMIN]`

**Query Params**：

| 参数 | 类型 | 说明 |
|------|------|------|
| courseId | Long | 按课程筛选 |
| reportType | Integer | 报告类型：1=课程报告 2=学校报告 |
| startTime | String | 开始时间（yyyy-MM-dd HH:mm:ss） |
| endTime | String | 结束时间 |
| pageNum | Integer | 默认 1 |
| pageSize | Integer | 默认 10 |

---

### 3.6 删除报告

`DELETE /api/v1/reports/{reportId}` 🔐 `[TEACHER/ADMIN]`

> ⚠️ 同时删除 OSS 文件，不可恢复。

---

## 4. 管理员配置（AdminConfigController）

**Base Path**：`/api/v1/admin/config`  
**权限要求**：`ADMIN`

> 用于配置素养画像计算的权重和等级阈值。

---

### 4.1 获取权重配置

`GET /api/v1/admin/config/weights` 🔐 `[ADMIN]`

**Response Data**：

```json
{
  "dimension_1": 0.2,
  "dimension_2": 0.2,
  "dimension_3": 0.2,
  "dimension_4": 0.2,
  "dimension_5": 0.2
}
```

---

### 4.2 更新权重配置

`PUT /api/v1/admin/config/weights` 🔐 `[ADMIN]`

**Request Body**（WeightsUpdateRequest）：

| 字段 | 类型 | 说明 |
|------|------|------|
| dimension_1 | BigDecimal | 理论素养权重 |
| dimension_2 | BigDecimal | 实践能力权重 |
| dimension_3 | BigDecimal | 价值认同权重 |
| dimension_4 | BigDecimal | 创新思维权重 |
| dimension_5 | BigDecimal | 社会责任感权重 |

> ⚠️ **五项权重之和必须等于 1.0**，否则返回错误。

---

### 4.3 获取等级阈值

`GET /api/v1/admin/config/thresholds` 🔐 `[ADMIN]`

**Response Data**：

```json
{ "excellent": 90.0, "good": 75.0, "pass": 60.0 }
```

---

### 4.4 更新等级阈值

`PUT /api/v1/admin/config/thresholds` 🔐 `[ADMIN]`

**Request Body**（ThresholdsUpdateRequest）：

| 字段 | 类型 | 说明 |
|------|------|------|
| excellent | BigDecimal | 优秀分数线 |
| good | BigDecimal | 良好分数线 |
| pass | BigDecimal | 合格分数线 |

> ⚠️ 校验规则：`excellent > good > pass > 0`

---

### 4.5 获取所有配置

`GET /api/v1/admin/config` 🔐 `[ADMIN]`

---

### 4.6 刷新配置缓存

`POST /api/v1/admin/config/refresh?configKey=dimension_weights` 🔐 `[ADMIN]`
