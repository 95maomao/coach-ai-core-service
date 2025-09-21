# CoachAI 用户管理 API 文档

## 概述

基于 `coach_ai_users` 数据表结构实现的用户注册和查询接口，支持完整的用户生命周期管理。

## 基础信息

- **Base URL**: `/api/coach-ai-users` (注意：由于application.yml中设置了context-path: /api)
- **Content-Type**: `application/json`
- **时间格式**: Unix 时间戳 (Long 类型)

## 数据模型

### CoachAiUser 实体字段

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `id` | Long | 是 | 主键，自动生成 |
| `username` | String | 是 | 用户昵称，唯一 |
| `passwordHash` | String | 否 | 密码哈希值 |
| `preferredSport` | Enum | 否 | 喜欢的运动类型 |
| `age` | Integer | 否 | 年龄 (0-255) |
| `height` | Integer | 否 | 身高 (cm) |
| `weight` | BigDecimal | 否 | 体重 (kg，精度2位小数) |
| `gender` | Enum | 否 | 性别，默认"不愿透露" |
| `createdAt` | Long | 是 | 创建时间戳 |
| `updatedAt` | Long | 是 | 更新时间戳 |

### 枚举类型

#### PreferredSport (运动类型)
- `RUNNING` - 跑步
- `SWIMMING` - 游泳
- `CYCLING` - 骑行
- `FITNESS` - 健身
- `BASKETBALL` - 篮球
- `FOOTBALL` - 足球
- `TENNIS` - 网球
- `BADMINTON` - 羽毛球
- `YOGA` - 瑜伽
- `DANCING` - 舞蹈

#### Gender (性别)
- `MALE` - 男
- `FEMALE` - 女
- `OTHER` - 其他
- `NOT_DISCLOSED` - 不愿透露

## API 接口

### 1. 用户注册

**POST** `/api/coach-ai-users/register`

#### 请求体
```json
{
  "username": "john_doe",
  "password": "password123",
  "preferredSport": "RUNNING",
  "age": 25,
  "height": 175,
  "weight": 70.5,
  "gender": "MALE"
}
```

#### 响应
```json
{
  "success": true,
  "message": "用户注册成功",
  "data": {
    "id": 1,
    "username": "john_doe",
    "preferredSport": "RUNNING",
    "age": 25,
    "height": 175,
    "weight": 70.5,
    "gender": "MALE",
    "createdAt": 1695225600000,
    "updatedAt": 1695225600000
  }
}
```

### 2. 根据ID查询用户

**GET** `/api/coach-ai-users/{id}`

#### 响应
```json
{
  "success": true,
  "data": {
    "id": 1,
    "username": "john_doe",
    "preferredSport": "RUNNING",
    "age": 25,
    "height": 175,
    "weight": 70.5,
    "gender": "MALE",
    "createdAt": 1695225600000,
    "updatedAt": 1695225600000
  }
}
```

### 3. 根据用户名查询用户

**GET** `/api/coach-ai-users/username/{username}`

### 4. 查询所有用户

**GET** `/api/coach-ai-users`

#### 响应
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "username": "john_doe",
      "preferredSport": "RUNNING",
      "age": 25,
      "height": 175,
      "weight": 70.5,
      "gender": "MALE",
      "createdAt": 1695225600000,
      "updatedAt": 1695225600000
    }
  ]
}
```

### 5. 根据年龄范围查询用户

**GET** `/api/coach-ai-users/age-range?minAge=20&maxAge=30`

### 6. 根据运动类型查询用户

**GET** `/api/coach-ai-users/sport/{sport}`

例如：`/api/coach-ai-users/sport/RUNNING`

### 7. 根据性别查询用户

**GET** `/api/coach-ai-users/gender/{gender}`

例如：`/api/coach-ai-users/gender/MALE`

### 8. 用户名模糊查询

**GET** `/api/coach-ai-users/search?username=john`

### 9. 更新用户信息

**PUT** `/api/coach-ai-users/{id}`

#### 请求体
```json
{
  "username": "john_doe_updated",
  "preferredSport": "SWIMMING",
  "age": 26,
  "height": 175,
  "weight": 72.0,
  "gender": "MALE"
}
```

### 10. 删除用户

**DELETE** `/api/coach-ai-users/{id}`

### 11. 获取运动类型枚举

**GET** `/api/coach-ai-users/sports`

#### 响应
```json
{
  "success": true,
  "data": {
    "RUNNING": "跑步",
    "SWIMMING": "游泳",
    "CYCLING": "骑行",
    "FITNESS": "健身",
    "BASKETBALL": "篮球",
    "FOOTBALL": "足球",
    "TENNIS": "网球",
    "BADMINTON": "羽毛球",
    "YOGA": "瑜伽",
    "DANCING": "舞蹈"
  }
}
```

### 12. 获取性别枚举

**GET** `/api/coach-ai-users/genders`

#### 响应
```json
{
  "success": true,
  "data": {
    "MALE": "男",
    "FEMALE": "女",
    "OTHER": "其他",
    "NOT_DISCLOSED": "不愿透露"
  }
}
```

### 13. 健康检查

**GET** `/api/coach-ai-users/health`

#### 响应
```json
{
  "status": "UP",
  "service": "coach-ai-core-service",
  "module": "CoachAI User Management",
  "timestamp": "2023-09-20T22:30:00"
}
```

## 错误响应格式

```json
{
  "success": false,
  "message": "错误信息描述",
  "data": null
}
```

## 注意事项

1. **密码安全**: 注册时的密码会使用 Base64 进行编码存储（简化处理）
2. **用户名唯一性**: 用户名必须唯一，重复注册会返回错误
3. **时间戳**: 所有时间字段使用 Unix 时间戳格式 (Long 类型，毫秒)
4. **数据验证**: 所有输入数据都会进行格式和范围验证
5. **默认值**: 性别字段默认为 "NOT_DISCLOSED"（不愿透露）

## 数据库表结构

对应的数据库表名为 `coach_ai_users`，包含所有上述字段，时间字段使用 BIGINT 类型存储时间戳。
