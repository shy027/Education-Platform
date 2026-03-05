# 课程服务 API 文档

**服务名**：`course-service`  
**网关路由前缀**：`/api/v1`

---

## 1. 课程管理（CourseController）

**Base Path**：`/api/v1/courses`

---

### 1.1 创建课程

`POST /api/v1/courses` 🔐 `[TEACHER/ADMIN]`

**Request Body**（CourseCreateRequest）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| courseName | String | ✅ | 课程名称 |
| description | String | ❌ | 课程简介 |
| cover | String | ❌ | 封面图 URL |
| subjectArea | String | ❌ | 学科领域 |
| joinType | Integer | ✅ | 加入方式：0=公开 1=审批 2=邀请码 |
| inviteCode | String | ❌ | 邀请码（joinType=2 时必填） |
| schoolId | Long | ❌ | 所属学校ID |

**Response Data**：`{ "data": 1001 }` （courseId）

---

### 1.2 更新课程

`PUT /api/v1/courses` 🔐

**Request Body**（CourseUpdateRequest）：包含 `id` + 需要更新的字段（同创建）

---

### 1.3 获取课程详情

`GET /api/v1/courses/{id}` 🔐

**Response Data**（CourseDetailResponse）：

```json
{
  "id": 1001,
  "courseName": "思政教育实践",
  "description": "...",
  "cover": "https://...",
  "status": 1,
  "joinType": 0,
  "teacherId": 100,
  "teacherName": "李老师",
  "memberCount": 50,
  "subjectArea": "思政教育",
  "auditStatus": 1,
  "createdTime": "2024-01-01 10:00:00"
}
```

---

### 1.4 分页查询课程列表

`GET /api/v1/courses` 🔐

**Query Params**：

| 参数 | 类型 | 说明 |
|------|------|------|
| keyword | String | 课程名模糊搜索 |
| schoolId | Long | 学校ID |
| subjectArea | String | 学科领域 |
| joinType | Integer | 加入方式 |
| status | Integer | 状态：0=草稿 1=发布 2=归档 |
| auditStatus | Integer | 审核状态：0=待审 1=通过 2=拒绝 |
| teacherId | Long | 教师ID |
| pageNum | Integer | 默认 1 |
| pageSize | Integer | 默认 10 |

---

### 1.5 修改课程状态

`PUT /api/v1/courses/{id}/status` 🔐

**Query Param**：`status`（Integer）

---

## 2. 章节管理（ChapterController）

**Base Path**：`/api/v1/courses/{courseId}/chapters`

---

### 2.1 获取章节树

`GET /api/v1/courses/{courseId}/chapters/tree` 🔐

**Response Data**（树形结构 `List<ChapterTreeResponse>`）：

```json
[
  {
    "id": 1, "title": "第一章", "sortOrder": 1,
    "children": [
      { "id": 2, "title": "1.1 节", "sortOrder": 1, "children": [] }
    ]
  }
]
```

---

### 2.2 创建章节

`POST /api/v1/courses/{courseId}/chapters` 🔐 `[TEACHER]`

**Request Body**（ChapterCreateRequest）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | String | ✅ | 章节标题 |
| parentId | Long | ❌ | 父章节ID（为空时为顶级章节） |
| sortOrder | Integer | ❌ | 排序值 |

---

### 2.3 更新章节

`PUT /api/v1/courses/{courseId}/chapters` 🔐（需包含 id）

---

### 2.4 删除章节

`DELETE /api/v1/courses/{courseId}/chapters/{id}` 🔐

---

## 3. 课件管理（CoursewareController & CoursewareFileController）

**Base Path**：`/api/v1`（注意路径略有不同）

---

### 3.1 上传课件文件（先上传文件，再创建课件）

**四种文件类型接口（均为 `multipart/form-data`，字段名 `file`）：**

| 接口 | 路径 | 说明 |
|------|------|------|
| 上传视频 | `POST /api/v1/courseware/files/video` 🔐 `[TEACHER]` | 返回 `{ fileUrl, fileSize, duration }` |
| 上传 PDF | `POST /api/v1/courseware/files/pdf` 🔐 `[TEACHER]` | 返回 `{ fileUrl, fileSize }` |
| 上传音频 | `POST /api/v1/courseware/files/audio` 🔐 `[TEACHER]` | 返回 `{ fileUrl, fileSize, duration }` |
| 上传 PPT | `POST /api/v1/courseware/files/ppt` 🔐 `[TEACHER]` | 返回 `{ fileUrl, fileSize }` |
| 上传封面 | `POST /api/v1/courseware/files/cover` 🔐 `[TEACHER]` | 返回 `{ fileUrl }` |

**FileUploadResponse**：`{ "fileUrl": "https://...", "fileSize": 1024000, "duration": 3600 }`

---

### 3.2 创建课件（登记元数据）

`POST /api/v1/courses/{courseId}/coursewares` 🔐 `[TEACHER]`

**Request Body**（CoursewareUploadRequest）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | String | ✅ | 课件标题 |
| wareType | Integer | ✅ | 类型：1=视频 2=文档(PDF) 3=PPT 4=音频 |
| fileUrl | String | ✅ | 文件上传后的 URL |
| coverUrl | String | ❌ | 封面图 URL |
| chapterId | Long | ❌ | 所属章节ID |
| description | String | ❌ | 简介 |
| duration | Integer | ❌ | 时长（秒），视频/音频有效 |
| sortOrder | Integer | ❌ | 排序 |

**Response Data**：`wareId`（Long）

> ⚠️ **流程**：先调用 3.1 上传文件获取 `fileUrl`，再调用此接口创建课件。

---

### 3.3 获取课件列表

`GET /api/v1/courses/{courseId}/coursewares` 🔐

**Query Params**：chapterId、wareType（1/2/3/4）、auditStatus（0=待审 1=通过 2=拒绝）、pageNum、pageSize

---

### 3.4 获取课件详情

`GET /api/v1/coursewares/{wareId}` 🔐

**Response Data**（包含学习进度 `progress` 字段）

---

### 3.5 更新 / 删除课件

- `PUT /api/v1/coursewares/{wareId}` 🔐
- `DELETE /api/v1/coursewares/{wareId}` 🔐

---

### 3.6 审核课件

`POST /api/v1/coursewares/{wareId}/audit` 🔐 `[ADMIN/SCHOOL_LEADER]`

**Query Param**：`auditStatus`（1=通过 2=拒绝）

---

### 3.7 记录学习进度

`POST /api/v1/coursewares/{wareId}/progress` 🔐

**Request Body**（ProgressRecordRequest）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| watchedDuration | Integer | ✅ | 已观看时长（秒） |
| totalDuration | Integer | ✅ | 总时长（秒） |
| isFinished | Boolean | ❌ | 是否完成 |

---

### 3.8 获取学习进度

`GET /api/v1/coursewares/{wareId}/progress` 🔐

---

### 3.9 获取课件所有学生进度统计（教师）

`GET /api/v1/coursewares/{wareId}/progress/students` 🔐 `[TEACHER]`

**Query Params**：pageNum、pageSize

---

### 3.10 获取学生课程学习进度（教师）

`GET /api/v1/courses/{courseId}/progress/student/{userId}` 🔐 `[TEACHER]`

**Query Params**：pageNum、pageSize

---

## 4. 任务管理（TaskController）

**Base Path**：`/api/v1/courses/{courseId}/tasks`

> 任务类型：`taskType` = 1→作业 2→考试 3→讨论

---

### 4.1 创建任务

`POST /api/v1/courses/{courseId}/tasks` 🔐 `[TEACHER]`

**Request Body**（TaskCreateRequest）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| taskName | String | ✅ | 任务名称 |
| taskType | Integer | ✅ | 1=作业 2=考试 3=讨论 |
| description | String | ❌ | 描述 |
| startTime | String | ❌ | 开始时间 |
| endTime | String | ❌ | 结束时间 |
| totalScore | Integer | ❌ | 总分 |
| duration | Integer | ❌ | 考试时长（分钟），考试任务使用 |

**Response Data**：taskId（Long）

---

### 4.2 查询 / 详情 / 更新 / 删除任务

| 接口 | 路径 |
|------|------|
| 分页查询 | `GET /api/v1/courses/{courseId}/tasks` |
| 详情 | `GET /api/v1/courses/{courseId}/tasks/{id}` |
| 更新 | `PUT /api/v1/courses/{courseId}/tasks`（body 含 id） |
| 删除 | `DELETE /api/v1/courses/{courseId}/tasks/{id}` |
| 修改状态 | `PUT /api/v1/courses/{courseId}/tasks/{id}/status?status=1` |

**任务状态**：0=草稿 1=发布 2=结束

---

## 5. 课程成员管理（CourseMemberController）

**Base Path**：`/api/v1/courses`

---

### 5.1 申请加入课程

`POST /api/v1/courses/{courseId}/join` 🔐

> 公开课直接加入，审批制课程需等待教师审批。

---

### 5.2 退出课程

`DELETE /api/v1/courses/{courseId}/quit` 🔐

---

### 5.3 审批成员

`PUT /api/v1/courses/{courseId}/members/{userId}/approve` 🔐 `[TEACHER]`

**Request Body**（ApproveMemberRequest）：

| 字段 | 类型 | 说明 |
|------|------|------|
| approve | Boolean | true=同意 false=拒绝 |
| reason | String | 拒绝原因 |

---

### 5.4 获取成员列表

`GET /api/v1/courses/{courseId}/members` 🔐

**Query Params**：

| 参数 | 类型 | 说明 |
|------|------|------|
| memberRole | Integer | 1=教师 2=助教 3=学生 |
| joinStatus | Integer | 0=待审批 1=已通过 2=已拒绝 |
| pageNum | Integer | 默认 1 |
| pageSize | Integer | 默认 10 |

---

### 5.5 获取我的课程

`GET /api/v1/courses/my-courses` 🔐

**Response Data**（MyCoursesResponse）：包含我教的课程 + 我加入的课程列表

---

### 5.6 添加 / 移除成员

- `POST /api/v1/courses/{courseId}/members` - Body：`{ "userId": 1001, "memberRole": 3 }`
- `DELETE /api/v1/courses/{courseId}/members/{userId}`

---

### 5.7 修改成员角色

`PUT /api/v1/courses/{courseId}/members/{userId}/role` 🔐

**Request Body**：`{ "memberRole": 2 }` （1=教师 2=助教 3=学生）

---

### 5.8 检查成员状态

`GET /api/v1/courses/{courseId}/members/check?userId=1001`

---

## 6. 学生考试（StudentExamController）

**Base Path**：`/api/v1/student/exams`

---

### 6.1 获取考试列表

`GET /api/v1/student/exams` 🔐

**Query Params**：courseId、status（0=未开始 1=进行中 2=已结束）、pageNum、pageSize

---

### 6.2 获取考试详情（含试卷）

`GET /api/v1/student/exams/{taskId}` 🔐

**Response Data**（PaperResponse）：包含题目列表、选项等

---

### 6.3 开始考试

`POST /api/v1/student/exams/{taskId}/start` 🔐

**Response Data**：`recordId`（Long，作答记录ID，后续提交答案使用）

> ⚠️ 开始后计时，超时自动提交。

---

## 7. 学生答题（AnswerController）

**Base Path**：`/api/v1/student/answers`

---

### 7.1 保存答案（自动保存用）

`POST /api/v1/student/answers/save` 🔐

**Request Body**（SaveAnswerRequest）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| recordId | Long | ✅ | 作答记录ID |
| questionId | Long | ✅ | 题目ID |
| answer | String | ✅ | 答案内容 |

---

### 7.2 提交答卷

`POST /api/v1/student/answers/{recordId}/submit` 🔐

---

### 7.3 获取答题进度

`GET /api/v1/student/answers/{recordId}` 🔐

---

## 8. 教师批改（GradingController）

**Base Path**：`/api/v1/grading`

---

### 8.1 获取待批改列表

`GET /api/v1/grading/pending?taskId=1001&pageNum=1&pageSize=10` 🔐 `[TEACHER]`

---

### 8.2 批改单题

`POST /api/v1/grading/grade` 🔐 `[TEACHER]`

**Request Body**（GradeAnswerRequest）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| answerId | Long | ✅ | 答案ID |
| score | Integer | ✅ | 得分 |
| comment | String | ❌ | 批改评语 |

---

### 8.3 发布成绩

`POST /api/v1/grading/{recordId}/publish` 🔐 `[TEACHER]`

---

### 8.4 获取批改结果

`GET /api/v1/grading/{recordId}` 🔐

---

## 9. 公告管理（AnnouncementController）

**Base Path**：`/api/v1/courses/{courseId}/announcements`

---

### 9.1 发布公告

`POST /api/v1/courses/{courseId}/announcements` 🔐 `[TEACHER]`

**Request Body**（AnnouncementCreateRequest）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | String | ✅ | 标题 |
| content | String | ✅ | 内容（支持富文本） |
| isTop | Integer | ❌ | 1=置顶 0=不置顶 |

---

### 9.2 查询 / 详情 / 更新 / 删除 / 置顶

| 接口 | 路径 |
|------|------|
| 分页查询 | `GET /api/v1/courses/{courseId}/announcements?keyword=&isTop=&pageNum=&pageSize=` |
| 详情 | `GET /api/v1/courses/{courseId}/announcements/{id}` |
| 更新 | `PUT /api/v1/courses/{courseId}/announcements`（body 含 id） |
| 删除 | `DELETE /api/v1/courses/{courseId}/announcements/{id}` |
| 置顶切换 | `PUT /api/v1/courses/{courseId}/announcements/{id}/top?isTop=1` |
