# 用户服务 API 文档

**服务名**：`user-service`  
**网关路由前缀**：`/api/v1`

---

## 1. 用户认证（AuthController）

**Base Path**：`/api/v1/auth`

> ⚠️ 注册/登录/发送验证码 **不需要** Authorization Header，其他接口需要。

---

### 1.1 用户注册

`POST /api/v1/auth/register`

**Request Body**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | ✅ | 用户名（唯一） |
| password | String | ✅ | 密码 |
| phone | String | ❌ | 手机号 |
| email | String | ❌ | 邮箱 |

**Response Data**：

```json
{ "userId": 1001, "username": "zhangsan" }
```

---

### 1.2 账号密码登录

`POST /api/v1/auth/login`

**Request Body**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | ✅ | 用户名 |
| password | String | ✅ | 密码 |

**Response Data**（LoginResponse）：

```json
{
  "token": "eyJhbGc...",
  "tokenType": "Bearer",
  "userId": 1001,
  "username": "zhangsan",
  "realName": "张三",
  "avatar": "https://oss.xxx.com/avatar/xxx.jpg",
  "roles": ["TEACHER"],
  "schoolId": 10,
  "schoolName": "示例大学"
}
```

> ⚠️ **Token 存储**：建议存储在 `localStorage` 或内存中，请求时在 `Authorization: Bearer <token>` Header 中携带。

---

### 1.3 手机号 + 密码登录

`POST /api/v1/auth/phone-password-login`

**Request Body**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| phone | String | ✅ | 手机号 |
| password | String | ✅ | 密码 |

**Response Data**：同 [1.2 登录响应](#12-账号密码登录)

---

### 1.4 手机号 + 验证码登录

`POST /api/v1/auth/phone-code-login`

**Request Body**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| phone | String | ✅ | 手机号 |
| code | String | ✅ | 短信验证码 |

**Response Data**：同 [1.2 登录响应](#12-账号密码登录)

---

### 1.5 发送验证码

`POST /api/v1/auth/send-code`

**Request Body**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| phone | String | ✅ | 手机号 |

**Response Data**：

```json
{ "message": "验证码已发送", "code": "123456", "tip": "开发模式,验证码有效期5分钟" }
```

> ⚠️ `code` 字段仅开发模式返回，生产不返回。验证码有效期 5 分钟。

---

### 1.6 获取当前用户信息

`GET /api/v1/auth/current-user` 🔐

**Response Data**（CurrentUserResponse）：

```json
{
  "userId": 1001,
  "username": "zhangsan",
  "realName": "张三",
  "avatar": "https://...",
  "phone": "138xxx",
  "email": "xxx@gmail.com",
  "roles": ["TEACHER"],
  "schoolId": 10,
  "schoolName": "示例大学",
  "status": 1
}
```

---

### 1.7 修改个人信息

`PUT /api/v1/auth/profile` 🔐

**Request Body**（UpdateProfileRequest）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| realName | String | ❌ | 真实姓名 |
| avatar | String | ❌ | 头像 URL（先调用文件上传接口获取） |
| email | String | ❌ | 邮箱 |
| phone | String | ❌ | 手机号 |

---

### 1.8 修改密码

`PUT /api/v1/auth/password` 🔐

**Request Body**（UpdatePasswordRequest）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| oldPassword | String | ✅ | 旧密码 |
| newPassword | String | ✅ | 新密码 |

---

## 2. 用户管理（UserManageController）

**Base Path**：`/api/v1/users/manage`  
**权限要求**：`ADMIN` 或 `SCHOOL_LEADER`

---

### 2.1 用户列表查询（分页）

`GET /api/v1/users/manage` 🔐 `[ADMIN/SCHOOL_LEADER]`

**Query Params**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | ❌ | 用户名模糊搜索 |
| realName | String | ❌ | 真实姓名模糊搜索 |
| phone | String | ❌ | 手机号 |
| email | String | ❌ | 邮箱 |
| roleId | Long | ❌ | 角色ID筛选 |
| status | Integer | ❌ | 状态：0=禁用 1=启用 |
| pageNum | Integer | ❌ | 默认 1 |
| pageSize | Integer | ❌ | 默认 10 |

**Response Data**：分页列表（UserManageResponse）

---

### 2.2 用户详情

`GET /api/v1/users/manage/{userId}` 🔐 `[ADMIN/SCHOOL_LEADER]`

---

### 2.3 更新用户状态

`PUT /api/v1/users/manage/{userId}/status` 🔐 `[ADMIN/SCHOOL_LEADER]`

**Request Body**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | Integer | ✅ | 0=禁用 1=启用 |

---

### 2.4 重置用户密码

`POST /api/v1/users/manage/{userId}/reset-password` 🔐 `[ADMIN/SCHOOL_LEADER]`

**Response Data**：

```json
{ "newPassword": "Abc12345", "message": "密码已重置,请妥善保管" }
```

---

### 2.5 批量导入用户

`POST /api/v1/users/manage/import` 🔐 `[ADMIN/SCHOOL_LEADER]`

**Request**：`multipart/form-data`，字段名 `file`（Excel 文件）

**Response Data**：

```json
{ "successCount": 50, "failCount": 2, "failDetails": ["第3行：用户名已存在"] }
```

---

### 2.6 下载导入模板

`GET /api/v1/users/manage/template` 🔐 `[ADMIN/SCHOOL_LEADER]`

> 直接返回 Excel 文件流，前端使用 `<a>` 标签下载或用 `blob` 接收。

---

### 2.7 导出用户列表

`GET /api/v1/users/manage/export` 🔐 `[ADMIN/SCHOOL_LEADER]`

**Query Params**：username、realName、phone、schoolId、status（同列表查询参数）

> 直接返回 Excel 文件流。

---

### 2.8 批量获取用户信息

`POST /api/v1/users/manage/batch`（无特殊权限要求）

**Request Body**：`[1001, 1002, 1003]`（userId 数组）

**Response Data**：`{ "1001": { UserManageResponse }, "1002": { ... } }`

---

## 3. 学校管理（SchoolController）

**Base Path**：`/api/v1/schools`

---

### 3.1 获取学校列表

`GET /api/v1/schools`

**Query Params**：

| 参数 | 类型 | 说明 |
|------|------|------|
| keyword | String | 搜索关键词 |
| province | String | 省份筛选 |
| pageNum | Integer | 默认 1 |
| pageSize | Integer | 默认 10 |

---

### 3.2 获取学校详情

`GET /api/v1/schools/{schoolId}`

---

### 3.3 加入学校

`POST /api/v1/schools/{schoolId}/join` 🔐

**Request Body**（JoinSchoolRequest）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| inviteCode | String | ❌ | 邀请码（如学校有邀请码限制） |

---

## 4. 角色管理（RoleController）

**Base Path**：`/api/v1/roles`  
CRUD 操作权限：`ADMIN`；获取所有角色无权限要求。

---

### 4.1 获取所有角色（不分页）

`GET /api/v1/roles/all`（无权限要求）

**Response Data**：`[ { "id": 1, "roleName": "管理员", "roleCode": "ADMIN" }, ... ]`

---

### 4.2 角色列表（分页）

`GET /api/v1/roles` 🔐 `[ADMIN]`

**Query Params**：roleName、roleCode、status、pageNum、pageSize

---

### 4.3 创建角色

`POST /api/v1/roles` 🔐 `[ADMIN]`

**Request Body**（RoleCreateRequest）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| roleName | String | ✅ | 角色名称 |
| roleCode | String | ✅ | 角色编码（英文大写，如 TEACHER） |
| description | String | ❌ | 描述 |

---

### 4.4 更新 / 删除角色

- `PUT /api/v1/roles/{roleId}` 🔐 `[ADMIN]`
- `DELETE /api/v1/roles/{roleId}` 🔐 `[ADMIN]`

---

## 5. 文件上传（FileController）

**Base Path**：`/api/v1/files`

---

### 5.1 上传头像

`POST /api/v1/files/avatar` 🔐

**Request**：`multipart/form-data`

| 字段 | 类型 | 说明 |
|------|------|------|
| file | File | 图片文件 |
| userId | Long | 用户ID |

**Response Data**：`{ "url": "https://oss.xxx.com/avatar/xxx.jpg" }`

---

### 5.2 上传文件

`POST /api/v1/files/upload` 🔐

**Request**：`multipart/form-data`

| 字段 | 类型 | 说明 |
|------|------|------|
| file | File | 任意文件 |
| folder | String | 存储目录（默认 temp） |

**Response Data**：`{ "url": "https://oss.xxx.com/temp/xxx.pdf" }`

---

### 5.3 删除文件

`DELETE /api/v1/files?url=<fileUrl>` 🔐
