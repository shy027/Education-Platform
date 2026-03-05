# 审核服务 API 文档

**服务名**：`audit-service`  
**网关路由前缀**：`/api/v1/audit`  
**权限要求**：所有接口均需 `ADMIN` 或 `SCHOOL_LEADER` 权限

---

## 接口说明

> **注意**：`audit-service` 提供**统一内容审核**能力，覆盖三类内容：
> - `COURSEWARE`：课件（course-service）
> - `POST`：讨论话题（community-service）
> - `COMMENT`：讨论观点（community-service）
> - `RESOURCE`：资源库（resource-service）
>
> 资源审核也可以通过 `resource-service` 的 `/api/v1/resources/{id}/audit` 直接操作，两者等价。

---

### 1. 查询待审核列表

`GET /api/v1/audit/pending` 🔐 `[ADMIN/SCHOOL_LEADER]`

**Query Params**：

| 参数 | 类型 | 说明 |
|------|------|------|
| contentType | String | COURSEWARE / POST / COMMENT / RESOURCE |
| riskLevel | Integer | 风险级别：1=低 2=中 3=高（AI 检测） |
| pageNum | Integer | 默认 1 |
| pageSize | Integer | 默认 10 |

**Response Data**（`PageResult<AuditRecordVO>`）：

```json
{
  "total": 20,
  "records": [{
    "id": 6001,
    "contentType": "COURSEWARE",
    "contentId": 1001,
    "contentTitle": "第一章 课件",
    "contentPreview": "...",
    "submitterId": 100,
    "submitterName": "李老师",
    "riskLevel": 1,
    "riskReason": "未检测到风险",
    "status": 0,
    "submittedTime": "2024-01-01 10:00:00"
  }]
}
```

---

### 2. 人工审核单条记录

`PUT /api/v1/audit/{recordId}` 🔐 `[ADMIN/SCHOOL_LEADER]`

**Request Body**（AuditRequest）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| auditResult | Integer | ✅ | 1=通过 2=拒绝 |
| auditComment | String | ❌ | 审核意见 |

---

### 3. 批量审核

`PUT /api/v1/audit/batch` 🔐 `[ADMIN/SCHOOL_LEADER]`

**Request Body**（BatchAuditRequest）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| recordIds | List\<Long\> | ✅ | 审核记录ID列表 |
| auditResult | Integer | ✅ | 1=通过 2=拒绝 |
| auditComment | String | ❌ | 统一审核意见 |

**Response Data**（BatchAuditResult）：

```json
{ "successCount": 10, "failCount": 0, "failDetails": [] }
```

---

### 4. 查询审核历史记录

`GET /api/v1/audit/records` 🔐 `[ADMIN/SCHOOL_LEADER]`

**Query Params**：

| 参数 | 类型 | 说明 |
|------|------|------|
| contentType | String | 内容类型筛选 |
| auditResult | Integer | 审核结果：1=通过 2=拒绝 |
| startDate | String | 开始日期（yyyy-MM-dd） |
| endDate | String | 结束日期（yyyy-MM-dd） |
| pageNum | Integer | 默认 1 |
| pageSize | Integer | 默认 10 |

**Response Data**：同待审核列表，但包含 `auditorId`、`auditorName`、`auditTime` 字段。
