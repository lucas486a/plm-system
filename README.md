# PLM System - 产品生命周期管理系统

一个基于 Spring Boot + Vue 3 的现代化产品生命周期管理（PLM）系统，专为制造业（机械/电子）设计，支持完整的 BOM 管理和工程变更流程。

## ✨ 核心功能

### 📦 零部件管理（Part Management）
- 零部件 CRUD 操作
- 主数据/修订版本分离模式（Master/Revision）
- 版本历史追踪
- 零部件搜索（按编号、名称、描述）

### 📄 文档管理（Document Management）
- 文档 CRUD 操作
- 文件上传/下载（支持大文件）
- 文档版本管理
- 文件元数据管理（大小、类型、路径）

### 🔧 BOM 管理（Bill of Materials）
- 多层级 BOM 支持（无限嵌套）
- BOM 树形展开
- Where-Used 反向查询
- 循环引用检测
- BOM 复制、比较、导出/导入（CSV）
- BOM 快照和成本计算

### 🔄 变更管理（Change Management）
- **ECR（工程变更请求）**：创建 → 提交 → 评估 → 批准/拒绝
- **ECO（工程变更订单）**：创建 → 提交 → 审批 → 应用 → 关闭
- 多阶段顺序审批（工程审核 → 经理审批）
- ECR 到 ECO 转换
- 变更影响分析

### 👥 用户管理（User Management）
- 用户 CRUD 操作
- 角色管理（RBAC）
- JWT 认证
- 登录/登出

### 📊 仪表盘（Dashboard）
- 关键指标统计（零部件数、文档数、待处理变更单）
- 最近活动时间线
- 快速操作入口

### 📋 审计日志（Audit Log）
- 所有实体变更自动记录
- 按实体、用户、操作、时间筛选
- JSONB 格式存储变更详情

---

## 🏗️ 技术栈

### 后端
| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 17 | 编程语言 |
| Spring Boot | 3.2.5 | 应用框架 |
| Spring Data JPA | - | 数据持久化 |
| Spring Security | - | 安全认证 |
| PostgreSQL | - | 生产数据库 |
| H2 | - | 开发/测试数据库 |
| Flyway | - | 数据库迁移 |
| Flowable | 7.0.1 | 工作流引擎 |
| JWT (jjwt) | 0.12.5 | 身份认证 |
| Lombok | - | 代码简化 |
| MapStruct | 1.5.5 | DTO 映射 |
| SpringDoc OpenAPI | 2.5.0 | API 文档 |

### 前端
| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.5 | 前端框架 |
| TypeScript | 5.7 | 类型安全 |
| Vite | 6.3 | 构建工具 |
| Ant Design Vue | 4.2 | UI 组件库 |
| Pinia | 3.0 | 状态管理 |
| Vue Router | 4.6 | 路由管理 |
| Axios | 1.15 | HTTP 客户端 |

---

## 🚀 快速开始

### 前置要求

- **Java 17+**（推荐 Microsoft OpenJDK）
- **Maven 3.9+**
- **Node.js 18+**（推荐 20+）
- **npm 9+**
- **PostgreSQL 14+**（生产环境）或使用 H2（开发环境）

### 1. 克隆项目

```bash
git clone <repository-url>
cd plm-system
```

### 2. 启动后端

#### 开发模式（使用 H2 内存数据库）

```bash
# Windows PowerShell
$env:SPRING_PROFILES_ACTIVE = "dev"
mvn spring-boot:run

# Linux/Mac
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run
```

#### 生产模式（使用 PostgreSQL）

1. 创建数据库：
```sql
CREATE DATABASE plm;
CREATE USER plm_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE plm TO plm_user;
```

2. 配置环境变量：
```bash
export SPRING_PROFILES_ACTIVE=prod
export DB_URL=jdbc:postgresql://localhost:5432/plm
export DB_USERNAME=plm_user
export DB_PASSWORD=your_password
export JWT_SECRET=your-super-secret-jwt-key-must-be-long-enough
```

3. 启动应用：
```bash
mvn spring-boot:run
```

后端将在 http://localhost:8080 启动。

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端将在 http://localhost:5173 启动。

### 4. 访问系统

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端 | http://localhost:5173 | Vue 3 应用 |
| 后端 API | http://localhost:8080 | REST API |
| Swagger UI | http://localhost:8080/swagger-ui.html | API 文档 |
| H2 Console | http://localhost:8080/h2-console | 数据库控制台（仅 dev） |
| Actuator | http://localhost:8080/actuator/health | 健康检查 |

### 5. 默认登录

- **用户名**: `admin`
- **密码**: `admin123`

---

## 📁 项目结构

```
plm-system/
├── src/                              # 后端源码
│   └── main/
│       ├── java/com/plm/
│       │   ├── aspect/              # AOP 切面（审计日志）
│       │   ├── config/              # 配置类
│       │   ├── controller/          # REST 控制器
│       │   ├── dto/                 # 数据传输对象
│       │   ├── entity/              # JPA 实体
│       │   ├── mapper/              # MapStruct 映射器
│       │   ├── repository/          # Spring Data 仓库
│       │   ├── security/            # 安全配置（JWT）
│       │   ├── service/             # 业务逻辑
│       │   ├── workflow/            # Flowable 工作流
│       │   └── PlmApplication.java  # 应用入口
│       └── resources/
│           ├── application.yml      # 主配置
│           ├── application-dev.yml  # 开发配置
│           ├── data.sql             # 初始数据
│           ├── db/migration/        # Flyway 迁移脚本
│           └── processes/           # BPMN 工作流定义
│
├── frontend/                         # 前端源码
│   ├── src/
│   │   ├── api/                     # API 客户端
│   │   ├── assets/                  # 静态资源
│   │   ├── components/              # 通用组件
│   │   ├── router/                  # 路由配置
│   │   ├── stores/                  # Pinia 状态
│   │   ├── types/                   # TypeScript 类型
│   │   ├── utils/                   # 工具函数
│   │   ├── views/                   # 页面组件
│   │   ├── App.vue                  # 根组件
│   │   └── main.ts                  # 入口文件
│   ├── index.html                   # HTML 模板
│   ├── package.json                 # 依赖配置
│   ├── tsconfig.json                # TypeScript 配置
│   └── vite.config.ts               # Vite 配置
│
├── pom.xml                           # Maven 配置
├── README.md                         # 项目文档
└── .gitignore                        # Git 忽略规则
```

---

## 🗄️ 数据库设计

### 核心实体关系

```
┌─────────────────────────────────────────────────────────────────┐
│                        PLM 数据模型                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────┐    has_revisions   ┌──────────────┐               │
│  │   Part   │───────────────────▶│ PartRevision │               │
│  └──────────┘                    └──────────────┘               │
│       │                                │                         │
│       │ referenced_by                  │ referenced_by           │
│       ▼                                ▼                         │
│  ┌──────────┐                    ┌──────────────┐               │
│  │ Document │────has_revisions──▶│DocRevision   │               │
│  └──────────┘                    └──────────────┘               │
│                                       │                         │
│                                       │ attached_to             │
│                                       ▼                         │
│  ┌──────────┐    contains    ┌──────────────┐                  │
│  │   BOM    │───────────────▶│   BOMItem    │                  │
│  └──────────┘                └──────────────┘                  │
│       │                            │                            │
│       │ belongs_to                 │ references                 │
│       ▼                            ▼                            │
│  ┌──────────┐                ┌──────────────┐                  │
│  │ Assembly │                │PartRevision  │                  │
│  └──────────┘                └──────────────┘                  │
│                                                                  │
│  ┌──────────┐    creates     ┌──────────────┐                  │
│  │   ECR    │───────────────▶│     ECO      │                  │
│  └──────────┘                └──────────────┘                  │
│                                    │                            │
│                                    │ has_approvals              │
│                                    ▼                            │
│                              ┌──────────────┐                  │
│                              │ ECOApproval  │                  │
│                              └──────────────┘                  │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 核心表

| 表名 | 说明 | 关键字段 |
|------|------|----------|
| `parts` | 零部件主表 | part_number, name, description |
| `part_revisions` | 零部件修订版本 | part_id, revision, lifecycle_state |
| `documents` | 文档主表 | document_number, title, document_type |
| `document_revisions` | 文档修订版本 | document_id, revision, file_path |
| `assemblies` | 组件/装配体 | part_number, name |
| `boms` | BOM 表头 | assembly_id, status |
| `bom_items` | BOM 行项 | bom_id, part_revision_id, quantity |
| `ecrs` | 工程变更请求 | ecr_number, status, priority |
| `ecos` | 工程变更订单 | eco_number, status, ecr_id |
| `eco_approvals` | ECO 审批记录 | eco_id, stage, approver_id, decision |
| `users` | 用户表 | username, password_hash, email |
| `roles` | 角色表 | name, description |
| `user_roles` | 用户角色关联 | user_id, role_id |
| `audit_logs` | 审计日志 | entity_type, entity_id, action |

---

## 🔄 工作流

### ECR 工作流

```
┌─────────┐    Submit    ┌──────────┐    Evaluate   ┌───────────┐
│  DRAFT  │─────────────▶│SUBMITTED │─────────────▶│ EVALUATED │
└─────────┘              └──────────┘              └───────────┘
                                                         │
                              ┌──────────────────────────┤
                              ▼                          ▼
                        ┌──────────┐              ┌───────────┐
                        │ REJECTED │              │ APPROVED  │
                        └──────────┘              └───────────┘
                                                         │
                                                         ▼
                                                  ┌───────────┐
                                                  │ CONVERTED │
                                                  │  (to ECO) │
                                                  └───────────┘
```

### ECO 工作流

```
┌─────────┐    Submit    ┌────────────┐   Approve    ┌───────────┐
│  DRAFT  │─────────────▶│IN_PROGRESS │────────────▶│ APPROVED  │
└─────────┘              └────────────┘              └───────────┘
     ▲                          │                         │
     │                          │ Reject                  │ Apply
     │                          ▼                         ▼
     │                    ┌──────────┐              ┌───────────┐
     └────────────────────│ REJECTED │              │  APPLIED  │
            Revise        └──────────┘              └───────────┘
                                                          │
                                                          │ Close
                                                          ▼
                                                    ┌───────────┐
                                                    │  CLOSED   │
                                                    └───────────┘
```

### ECO 多阶段审批

```
ENGINEERING_REVIEW → MANAGER_APPROVAL → APPROVED
```

---

## 🔌 API 端点

### 认证
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 用户登录 |

### 零部件
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/parts` | 获取零部件列表（分页） |
| GET | `/api/parts/{id}` | 获取零部件详情 |
| GET | `/api/parts/number/{partNumber}` | 按编号查询 |
| GET | `/api/parts/search?query={query}` | 搜索零部件 |
| POST | `/api/parts` | 创建零部件 |
| PUT | `/api/parts/{id}` | 更新零部件 |
| DELETE | `/api/parts/{id}` | 删除零部件 |
| GET | `/api/parts/{id}/revisions` | 获取版本历史 |
| POST | `/api/parts/{id}/revisions` | 创建新版本 |

### 文档
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/documents` | 获取文档列表 |
| GET | `/api/documents/{id}` | 获取文档详情 |
| POST | `/api/documents` | 创建文档 |
| PUT | `/api/documents/{id}` | 更新文档 |
| DELETE | `/api/documents/{id}` | 删除文档 |
| POST | `/api/documents/{id}/revisions/{revId}/files` | 上传文件 |
| GET | `/api/documents/{id}/revisions/{revId}/files/{fileName}` | 下载文件 |

### BOM
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/boms` | 获取 BOM 列表 |
| GET | `/api/boms/{id}` | 获取 BOM 详情 |
| POST | `/api/boms` | 创建 BOM |
| PUT | `/api/boms/{id}` | 更新 BOM |
| DELETE | `/api/boms/{id}` | 删除 BOM |
| GET | `/api/boms/{id}/explode` | BOM 树形展开 |
| GET | `/api/boms/{id}/items` | 获取 BOM 行项 |
| POST | `/api/boms/{id}/items` | 添加 BOM 行项 |
| GET | `/api/parts/{id}/where-used` | Where-Used 查询 |
| POST | `/api/boms/{id}/copy` | 复制 BOM |
| GET | `/api/boms/{id}/cost` | 计算成本 |
| GET | `/api/boms/{id}/export` | 导出 CSV |
| POST | `/api/boms/import` | 导入 CSV |

### 变更管理
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/ecrs` | 获取 ECR 列表 |
| POST | `/api/ecrs` | 创建 ECR |
| POST | `/api/ecrs/{id}/submit` | 提交 ECR |
| POST | `/api/ecrs/{id}/evaluate` | 评估 ECR |
| POST | `/api/ecrs/{id}/approve` | 批准 ECR |
| POST | `/api/ecrs/{id}/reject` | 拒绝 ECR |
| POST | `/api/ecrs/{id}/convert-to-eco` | 转换为 ECO |
| GET | `/api/ecos` | 获取 ECO 列表 |
| POST | `/api/ecos` | 创建 ECO |
| POST | `/api/ecos/{id}/submit` | 提交 ECO |
| POST | `/api/ecos/{id}/approve` | 审批 ECO |
| POST | `/api/ecos/{id}/reject` | 拒绝 ECO |
| POST | `/api/ecos/{id}/apply` | 应用 ECO |
| POST | `/api/ecos/{id}/close` | 关闭 ECO |

### 用户管理
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/users` | 获取用户列表 |
| GET | `/api/users/{id}` | 获取用户详情 |
| POST | `/api/users` | 创建用户 |
| PUT | `/api/users/{id}` | 更新用户 |
| DELETE | `/api/users/{id}` | 删除用户 |
| POST | `/api/users/{id}/roles` | 分配角色 |
| GET | `/api/roles` | 获取角色列表 |

### 仪表盘
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/dashboard/stats` | 获取统计数据 |

### 审计日志
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/audit-logs` | 获取审计日志 |
| GET | `/api/audit-logs/{id}` | 获取日志详情 |
| GET | `/api/audit-logs/entity/{type}/{id}` | 获取实体历史 |

---

## ⚙️ 配置

### 环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `SPRING_PROFILES_ACTIVE` | `default` | 激活的配置文件 |
| `DB_URL` | `jdbc:postgresql://localhost:5432/plm` | 数据库连接 URL |
| `DB_USERNAME` | `postgres` | 数据库用户名 |
| `DB_PASSWORD` | `postgres` | 数据库密码 |
| `JWT_SECRET` | 见 application.yml | JWT 签名密钥 |
| `JWT_EXPIRATION` | `86400000` | JWT 过期时间（毫秒） |
| `SERVER_PORT` | `8080` | 服务端口 |

### 配置文件

- `application.yml` - 主配置（生产环境）
- `application-dev.yml` - 开发配置（H2 数据库）

---

## 🛠️ 开发指南

### 构建项目

```bash
# 后端构建
mvn clean package

# 前端构建
cd frontend
npm run build
```

### 运行测试

```bash
# 后端测试
mvn test

# 前端测试
cd frontend
npm run test
```

### 代码规范

- 使用 Lombok 减少样板代码
- 使用 MapStruct 进行 DTO 映射
- 遵循 RESTful API 设计规范
- 使用 OpenAPI 注解生成 API 文档

### 数据库迁移

```bash
# 执行迁移
mvn flyway:migrate

# 查看迁移状态
mvn flyway:info

# 修复迁移
mvn flyway:repair
```

---

## 🚢 部署

### Docker 部署（推荐）

```dockerfile
# 后端 Dockerfile
FROM eclipse-temurin:17-jre-jammy
COPY target/plm-system-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
# 构建镜像
docker build -t plm-system .

# 运行容器
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL=jdbc:postgresql://db:5432/plm \
  -e DB_USERNAME=plm \
  -e DB_PASSWORD=secret \
  plm-system
```

### Docker Compose

```yaml
version: '3.8'
services:
  db:
    image: postgres:14
    environment:
      POSTGRES_DB: plm
      POSTGRES_USER: plm
      POSTGRES_PASSWORD: secret
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"

  backend:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DB_URL: jdbc:postgresql://db:5432/plm
      DB_USERNAME: plm
      DB_PASSWORD: secret
      JWT_SECRET: your-production-secret-key
    depends_on:
      - db

  frontend:
    build: ./frontend
    ports:
      - "80:80"
    depends_on:
      - backend

volumes:
  postgres_data:
```

---

## 📝 更新日志

### v0.1.0 (2026-04-29)
- 初始版本发布
- 核心数据模型（Part、Document、BOM、ECR/ECO）
- RESTful API（80+ 端点）
- Vue 3 前端界面
- JWT 认证
- Flowable 工作流集成
- 审计日志

