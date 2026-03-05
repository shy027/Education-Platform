# 社区服务 API 文档

**服务名**：`community-service`  
**网关路由前缀**：`/api/v1/community`

---

## 1. 讨论话题（PostController）

**Base Path**：`/api/v1/community/posts`

> **业务说明**：教师创建讨论话题，学生在话题下通过"观点（Comment）"接口发表内容（支持树形回复）。

---

### 1.1 创建讨论话题

`POST /api/v1/community/posts` 🔐 `[TEACHER]`

**Request Body**（CreatePostRequest）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| courseId | Long | ✅ | 所属课程ID |
| title | String | ✅ | 话题标题 |
| content | String | ❌ | 话题内容（支持富文本） |

**Response Data**（PostDetailResponse）：

```json
{
  "id": 2001,
  "courseId": 1001,
  "title": "关于思政精神的讨论",
  "content": "...",
  "authorId": 100,
  "authorName": "李老师",
  "likeCount": 5,
  "commentCount": 12,
  "isTop": 0,
  "isEssence": 0,
  "createdTime": "2024-01-01 10:00:00"
}
```

---

### 1.2 话题列表

`GET /api/v1/community/posts` 🔐

**Query Params**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| courseId | Long | ✅ | 课程ID |
| keyword | String | ❌ | 搜索关键词 |
| isTop | Integer | ❌ | 1=只看置顶 |
| isEssence | Integer | ❌ | 1=只看精华 |
| pageNum | Integer | ❌ | 默认 1 |
| pageSize | Integer | ❌ | 默认 10 |

---

### 1.3 我的帖子

`GET /api/v1/community/posts/my` 🔐

**Query Params**：keyword、pageNum、pageSize

---

### 1.4 我的点赞

`GET /api/v1/community/posts/my/likes` 🔐

**Query Params**：pageNum、pageSize

---

### 1.5 话题详情

`GET /api/v1/community/posts/{postId}` 🔐

---

### 1.6 编辑话题

`PUT /api/v1/community/posts/{postId}` 🔐（仅作者）

**Request Body**（UpdatePostRequest）：title、content

---

### 1.7 删除话题

`DELETE /api/v1/community/posts/{postId}` 🔐（作者或教师）

---

### 1.8 置顶 / 取消置顶

`PUT /api/v1/community/posts/{postId}/top?isTop=1` 🔐 `[TEACHER]`

| isTop | 说明 |
|-------|------|
| 1 | 置顶 |
| 0 | 取消置顶 |

---

### 1.9 设为精华 / 取消精华

`PUT /api/v1/community/posts/{postId}/essence?isEssence=1` 🔐 `[TEACHER]`

---

## 2. 观点（评论）管理（CommentController）

**Base Path**：`/api/v1/community/comments`

> **业务说明**：观点支持二级树形回复。`parentId` 为空时发表一级观点，有 `parentId` 时为回复。

---

### 2.1 发表观点

`POST /api/v1/community/comments` 🔐

**Request Body**（CreateCommentRequest）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| postId | Long | ✅ | 所属话题ID |
| content | String | ✅ | 内容 |
| parentId | Long | ❌ | 父观点ID（回复时填写） |

**Response Data**（CommentDetailResponse）：

```json
{
  "id": 3001,
  "postId": 2001,
  "content": "我认为...",
  "authorId": 200,
  "authorName": "张同学",
  "parentId": null,
  "likeCount": 2,
  "children": [],
  "createdTime": "2024-01-01 11:00:00"
}
```

---

### 2.2 观点列表（树形）

`GET /api/v1/community/comments` 🔐

**Query Params**：

| 参数 | 说明 |
|------|------|
| postId | 话题ID（必填） |
| parentId | 指定父ID查询子回复（可选） |
| pageNum | 默认 1 |
| pageSize | 默认 10 |

---

### 2.3 我的观点

`GET /api/v1/community/comments/my` 🔐  
**Query Params**：pageNum、pageSize

---

### 2.4 删除观点

`DELETE /api/v1/community/comments/{commentId}` 🔐（作者或教师）

---

## 3. 点赞（LikeController）

**Base Path**：`/api/v1/community/likes`

> 点赞为开关式（点一次点赞，再点一次取消点赞）。

---

### 3.1 话题点赞 / 取消

`POST /api/v1/community/likes/posts/{postId}` 🔐

**Response Data**（LikeResponse）：

```json
{ "liked": true, "likeCount": 6 }
```

---

### 3.2 观点点赞 / 取消

`POST /api/v1/community/likes/comments/{commentId}` 🔐

---

## 4. 小组管理（GroupController）

**Base Path**：`/api/v1/community/groups`

---

### 4.1 创建小组

`POST /api/v1/community/groups` 🔐 `[TEACHER]`

**Request Body**（CreateGroupRequest）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| courseId | Long | ✅ | 所属课程ID |
| groupName | String | ✅ | 小组名称 |
| description | String | ❌ | 小组描述 |
| maxMembers | Integer | ❌ | 最大成员数 |

---

### 4.2 查询小组列表

`GET /api/v1/community/groups` 🔐

**Query Params**：courseId（必填）、status（0=招募中 1=进行中 2=已解散）、pageNum、pageSize

---

### 4.3 获取小组详情

`GET /api/v1/community/groups/{groupId}` 🔐

---

### 4.4 更新小组信息

`PUT /api/v1/community/groups/{groupId}` 🔐（创建者或教师）

**Request Body**（UpdateGroupRequest）：groupName、description、maxMembers

---

### 4.5 解散小组

`DELETE /api/v1/community/groups/{groupId}` 🔐（创建者或教师）

---

### 4.6 申请加入小组

`POST /api/v1/community/groups/{groupId}/apply` 🔐

---

### 4.7 审批加入申请（教师）

`PUT /api/v1/community/groups/{groupId}/members/{memberId}/approve` 🔐 `[TEACHER]`

**Request Body**（ApproveJoinRequest）：

| 字段 | 类型 | 说明 |
|------|------|------|
| approveStatus | Integer | 1=同意 2=拒绝 |

---

### 4.8 退出小组

`DELETE /api/v1/community/groups/{groupId}/quit` 🔐

---

### 4.9 查询小组成员列表

`GET /api/v1/community/groups/{groupId}/members` 🔐

**Query Params**：pageNum、pageSize

---

### 4.10 查询待审批申请（教师）

`GET /api/v1/community/groups/{groupId}/requests` 🔐 `[TEACHER]`

**Query Params**：pageNum、pageSize

---

### 4.11 教师手动添加 / 移除成员

- `POST /api/v1/community/groups/{groupId}/members/{targetUserId}` 🔐 `[TEACHER]`
- `DELETE /api/v1/community/groups/{groupId}/members/{targetUserId}` 🔐 `[TEACHER]`

---

## 5. 小组协作文档（GroupDocumentController）

**Base Path**：`/api/v1/community/groups/{groupId}/documents`

> 协作文档由教师创建，小组成员均可编辑（富文本内容）。

---

### 5.1 创建文档

`POST /api/v1/community/groups/{groupId}/documents` 🔐 `[TEACHER]`

**Request Body**（CreateDocumentRequest）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | String | ✅ | 文档标题 |
| content | String | ❌ | 初始内容（富文本 HTML） |

**Response Data**：documentId（Long）

---

### 5.2 更新文档内容

`PUT /api/v1/community/groups/{groupId}/documents/{documentId}` 🔐（小组成员）

**Request Body**（UpdateDocumentRequest）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| content | String | ✅ | 富文本内容（完整替换） |
| editSummary | String | ❌ | 本次编辑摘要 |

> ⚠️ 每次更新会生成一条历史记录。前端建议实现自动保存（防抖 2s）。

---

### 5.3 获取文档详情

`GET /api/v1/community/groups/{groupId}/documents/{documentId}` 🔐

**Response Data**（DocumentDetailResponse）：

```json
{
  "id": 4001,
  "groupId": 3001,
  "title": "小组总结文档",
  "content": "<p>...</p>",
  "creatorId": 100,
  "lastEditorId": 201,
  "lastEditorName": "张同学",
  "createdTime": "...",
  "updatedTime": "..."
}
```

---

### 5.4 获取编辑历史

`GET /api/v1/community/groups/{groupId}/documents/{documentId}/history` 🔐

**Query Params**：pageNum、pageSize

**Response Data**（DocumentHistoryResponse 列表）：包含每次编辑的版本、编辑人、时间

---

### 5.5 删除文档

`DELETE /api/v1/community/groups/{groupId}/documents/{documentId}` 🔐 `[TEACHER]`

---

## 6. 小组话题（GroupTopicController）

**Base Path**：`/api/v1/community/groups`

> 小组内部的话题讨论（区别于课程级的讨论话题）。

---

### 6.1 创建话题（教师）

`POST /api/v1/community/groups/{groupId}/topics` 🔐 `[TEACHER]`

**Request Body**（CreateTopicRequest）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | String | ✅ | 话题标题 |
| description | String | ❌ | 说明 |
| endTime | String | ❌ | 截止时间 |

---

### 6.2 查询小组话题列表

`GET /api/v1/community/groups/{groupId}/topics` 🔐

**Query Params**：pageNum、pageSize

---

### 6.3 查询课程所有话题

`GET /api/v1/community/groups/courses/{courseId}/topics` 🔐

**Query Params**：pageNum、pageSize

---

### 6.4 获取话题详情

`GET /api/v1/community/groups/{groupId}/topics/{topicId}` 🔐

---

### 6.5 更新 / 删除话题（教师）

- `PUT /api/v1/community/groups/{groupId}/topics/{topicId}` 🔐 `[TEACHER]`
- `DELETE /api/v1/community/groups/{groupId}/topics/{topicId}` 🔐 `[TEACHER]`
