# 资源服务 API 文档

**服务名**：`resource-service`  
**网关路由前缀**：`/api/v1`

---

## 1. 资源管理（ResourceController）

**Base Path**：`/api/v1/resources`

### 资源状态枚举

| 状态值 | 说明 |
|--------|------|
| 0 | 草稿（教师创建，未提交） |
| 1 | 待审核（已提交，等待管理员审核） |
| 2 | 已发布（审核通过） |
| 3 | 已下架 |
| 4 | 审核拒绝 |

---

### 1.1 创建资源

`POST /api/v1/resources` 🔐 `[TEACHER/ADMIN/SCHOOL_LEADER]`

**Request Body**（ResourceCreateRequest）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | String | ✅ | 资源标题 |
| content | String | ❌ | 富文本内容 |
| coverUrl | String | ❌ | 封面图 URL |
| categoryId | Long | ❌ | 分类ID |
| tagIds | List\<Long\> | ❌ | 标签ID列表 |
| resourceType | Integer | ✅ | 类型：1=文章 2=视频 3=文档 4=音频 |
| fileUrl | String | ❌ | 附件 URL（非文章类型） |
| summary | String | ❌ | 摘要 |

**Response Data**：resourceId（Long）

> ⚠️ **创建角色差异**：
> - **管理员** 创建的资源直接发布（status=2）
> - **教师** 创建的资源为草稿（status=0），需手动提交审核

---

### 1.2 更新资源

`PUT /api/v1/resources/{id}` 🔐 `[TEACHER/ADMIN/SCHOOL_LEADER]`

**Request Body**（ResourceUpdateRequest）：同创建，不需要 resourceType

> ⚠️ 只有草稿状态（status=0）或被拒绝（status=4）的资源可以修改。

---

### 1.3 删除资源

`DELETE /api/v1/resources/{id}` 🔐 `[TEACHER/ADMIN/SCHOOL_LEADER]`

> ⚠️ 逻辑删除，已发布资源不允许直接删除。

---

### 1.4 获取资源详情

`GET /api/v1/resources/{id}`（无权限要求）

**Response Data**（ResourceDetailResponse）：

```json
{
  "id": 5001,
  "title": "思政教育案例分析",
  "content": "<p>...</p>",
  "coverUrl": "https://...",
  "resourceType": 1,
  "status": 2,
  "categoryId": 10,
  "categoryName": "思政理论",
  "tags": [{ "id": 1, "tagName": "家国情怀" }],
  "creatorId": 100,
  "creatorName": "李老师",
  "viewCount": 150,
  "fileUrl": null,
  "createdTime": "2024-01-01 10:00:00"
}
```

> ⚠️ 每次调用 getDetail 会自动 +1 浏览量。

---

### 1.5 分页查询资源列表

`GET /api/v1/resources`（无权限要求）

**Query Params**：

| 参数 | 类型 | 说明 |
|------|------|------|
| keyword | String | 标题模糊搜索 |
| categoryId | Long | 按分类筛选 |
| status | Integer | 状态筛选（前台一般只查询 status=2） |
| creatorId | Long | 创建者ID |
| tagId | Long | 按标签筛选 |
| pageNum | Integer | 默认 1 |
| pageSize | Integer | 默认 10 |

**Response Data**（ResourceResponse）：

```json
{
  "id": 5001,
  "title": "...",
  "coverUrl": "...",
  "summary": "...",
  "resourceType": 1,
  "status": 2,
  "viewCount": 150,
  "categoryName": "思政理论",
  "creatorName": "李老师",
  "createdTime": "..."
}
```

---

### 1.6 提交审核

`POST /api/v1/resources/{id}/submit` 🔐 `[TEACHER/ADMIN/SCHOOL_LEADER]`

> 将草稿（status=0）或被拒绝（status=4）的资源提交审核（status→1）。

---

### 1.7 审核资源

`POST /api/v1/resources/{id}/audit` 🔐 `[ADMIN/SCHOOL_LEADER]`

**Request Body**（ResourceAuditRequest）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| auditStatus | Integer | ✅ | 2=通过 4=拒绝 |
| auditComment | String | ❌ | 审核意见 |

---

### 1.8 获取待审核资源列表

`GET /api/v1/resources/pending` 🔐 `[ADMIN/SCHOOL_LEADER]`

**Query Params**：pageNum、pageSize

---

### 1.9 获取资源审核历史

`GET /api/v1/resources/{id}/audit-logs`（无权限要求）

**Response Data**（`List<AuditLogResponse>`）：

```json
[{
  "id": 1,
  "auditorId": 1,
  "auditorName": "管理员",
  "auditStatus": 2,
  "auditComment": "内容优质，审核通过",
  "auditTime": "2024-01-02 09:00:00"
}]
```

---

### 1.10 下架资源

`POST /api/v1/resources/{id}/offline` 🔐 `[ADMIN/SCHOOL_LEADER]`

---

## 2. 资源分类管理（CategoryController）

**Base Path**：`/api/v1/categories`

---

### 2.1 获取分类树（无权限）

`GET /api/v1/categories/tree`

**Response Data**（`List<CategoryResponse>` 树形结构）：

```json
[{
  "id": 10,
  "categoryName": "思政理论",
  "parentId": null,
  "sortOrder": 1,
  "children": [
    { "id": 11, "categoryName": "马克思主义", "parentId": 10, "children": [] }
  ]
}]
```

---

### 2.2 获取子分类列表

`GET /api/v1/categories/{parentId}/children`（无权限）

---

### 2.3 创建 / 更新 / 删除分类

> 权限：`ADMIN` 或 `SCHOOL_LEADER`

| 接口 | 路径 |
|------|------|
| 创建 | `POST /api/v1/categories`（body: { categoryName, parentId, sortOrder }） |
| 更新 | `PUT /api/v1/categories/{id}` |
| 删除 | `DELETE /api/v1/categories/{id}` |

---

## 3. 思政元素标签管理（TagController）

**Base Path**：`/api/v1/tags`

---

### 3.1 获取所有启用标签（常用，无分页）

`GET /api/v1/tags/enabled`（无需特殊权限）

**Response Data**（`List<TagResponse>`）：

```json
[{ "id": 1, "tagName": "家国情怀", "categoryId": 10, "status": 1 }]
```

> ⚠️ **资源创建/编辑时，前端调用此接口获取可选标签列表。**

---

### 3.2 标签列表（分页）

`GET /api/v1/tags` 🔐 `[TEACHER/ADMIN/SCHOOL_LEADER]`

**Query Params**：tagName、categoryId、status（1=启用）、pageNum、pageSize

---

### 3.3 创建 / 更新 / 删除标签

> 权限：`ADMIN` 或 `SCHOOL_LEADER`

| 接口 | 路径 | Request Body |
|------|------|------|
| 创建 | `POST /api/v1/tags` | `{ tagName, categoryId, description }` |
| 更新 | `PUT /api/v1/tags/{tagId}` | 同创建 |
| 删除 | `DELETE /api/v1/tags/{tagId}` | 无 |

---

## 4. 资源文件上传（FileUploadController）

**Base Path**：`/api/v1/upload`

> ⚠️ 此接口专用于资源库的**富文本图片**和**附件**上传（视频、PDF 等），网关路由：`/api/v1/upload/**` → resource-service。

| 接口 | 路径 | 说明 |
|------|------|------|
| 上传图片 | `POST /api/v1/upload/image` 🔐 | 富文本内嵌图片（jpg/png/gif） |
| 上传视频 | `POST /api/v1/upload/video` 🔐 | 资源库视频文件 |
| 上传 PDF | `POST /api/v1/upload/pdf` 🔐 | PDF 附件文档 |

**Request**：`multipart/form-data`，字段名 `file`

**Response Data**（AttachmentUploadResponse）：

```json
{ "url": "https://oss.xxx.com/resources/xxx.pdf", "fileName": "xxx.pdf", "fileSize": 102400 }
```
