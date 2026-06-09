# 🎫 漫GO票务系统 (Mygo Ticketing Platform)

基于 Spring Boot + Vue.js 构建的高并发票务平台，集成分布式消息队列、Redis 原子锁座、AI 智能助手和抽奖系统。

> 演示项目 — 参考大麦网票务系统架构设计

## 技术栈

| 层次         | 技术                                                    |
| ------------ | ------------------------------------------------------- |
| **后端**     | Java 17, Spring Boot 3.5, MyBatis, ShardingSphere       |
| **前端**     | Vue 3, Vite, Element Plus, Vue Router                   |
| **消息队列** | Apache RocketMQ（异步订单处理、延迟队列）               |
| **缓存与锁** | Redis + Lua 脚本实现原子锁座                            |
| **人工智能** | Spring AI, DeepSeek LLM, 阿里云 DashScope ASR, MCP 协议 |
| **数据库**   | MySQL（ShardingSphere 分库分表）                        |
| **基础设施** | Nacos, HikariCP, Feign, Sentinel                        |

## 功能特性

### 核心购票
- **高并发锁座** — Redis Lua 脚本保证原子性锁座/释放，避免超卖
- **异步订单流** — RocketMQ 解耦订单创建，延迟消息处理支付超时自动释放
- **库存流水追踪** — MQ 异步记录库存变更日志，完整审计链路

### AI 智能助手
- **自然语言搜票** — LLM 意图识别（`__TICKET_INTENT__` 协议，从对话中提取购票意图）
- **语音识别** — 阿里云 DashScope ASR 接口集成
- **向量检索** — 基于语义的节目/活动检索
- **状态机对话管理** — 引导式多轮对话
- **MCP 工具调用** — Spring AI MCP 客户端接入外部工具

### 抽奖系统
- 策略模式 + 工厂模式，支持多种抽奖算法
- 默认抽奖、末尾抽奖、权重抽奖
- 实物/虚拟奖品异步发放

### 其他
- 布隆过滤器缓存优化
- ShardingSphere 水平分库分表
- Feign + Sentinel 服务容错

## 系统架构

```
┌─────────────┐     ┌─────────────────────────────────────┐
│  Vue 3 前端  │────▶│       Spring Boot 后端              │
│  (Vite)      │     │  ┌─────────┐  ┌──────────────────┐  │
└─────────────┘     │  │Controller│─▶│   Service 层      │  │
                    │  └─────────┘  └───────┬──────────┘  │
                    │                       │              │
                    │  ┌────────────────────▼──────────┐   │
                    │  │   Redis (Lua 锁座 + 缓存)      │   │
                    │  └───────────────────────────────┘   │
                    │  ┌───────────────────────────────┐   │
                    │  │   RocketMQ (异步消息/延迟队列)  │   │
                    │  └───────────────────────────────┘   │
                    │  ┌───────────────────────────────┐   │
                    │  │   MySQL (ShardingSphere 分库)   │   │
                    │  └───────────────────────────────┘   │
                    │  ┌───────────────────────────────┐   │
                    │  │   Spring AI / LLM / MCP       │   │
                    │  └───────────────────────────────┘   │
                    └─────────────────────────────────────┘
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0+
- Redis 6+
- RocketMQ 5.x
- Nacos 2.x（可选）

### 配置

将配置模板复制为 `application.yaml` 并填入你的凭据：

```bash
cp src/main/resources/application-template.yaml src/main/resources/application.yaml
# 编辑 application.yaml 配置数据库、Redis、API Key 等
```

### 启动后端

```bash
mvn clean install -DskipTests
mvn spring-boot:run
```

服务启动在 `http://localhost:6086`

### 启动前端

```bash
cd frontend-vue
npm install
npm run dev
```

前端运行在 `http://localhost:5173`

### 依赖服务

| 服务     | 默认地址                            |
| -------- | ----------------------------------- |
| MySQL    | localhost:3306 / 数据库: demotest01 |
| Redis    | localhost:6379                      |
| RocketMQ | localhost:9876                      |
| Nacos    | localhost:8848                      |

## API 接口

| 方法 | 路径                   | 说明                |
| ---- | ---------------------- | ------------------- |
| POST | `/ticket/seat/create`  | 创建订单并锁座      |
| GET  | `/ticket/seat/release` | 释放座位            |
| POST | `/api/ai/chat`         | AI 对话（SSE 流式） |
| POST | `/api/ai/chat/process` | AI 对话 + 意图解析  |
| GET  | `/api/ai/ping`         | AI 服务健康检查     |

## 项目结构

```
demo/
├── src/main/java/com/
│   ├── example/
│   │   ├── controller/       # REST 接口
│   │   ├── service/          # 业务逻辑 + RocketMQ 消费者
│   │   ├── mapper/           # MyBatis 数据映射
│   │   ├── entity/           # 实体类
│   │   ├── dto/              # 数据传输对象
│   │   ├── DrawStrategy/     # 抽奖策略模式
│   │   ├── mq/               # MQ 回调
│   │   ├── constant/         # 常量
│   │   ├── core/             # 工具类
│   │   └── redis/            # Redis 锁测试
│   └── Ai/
│       ├── Controller/       # AI 接口
│       ├── Service/          # AI 服务实现
│       ├── config/           # Spring AI、MCP、CORS 配置
│       ├── state/            # 对话状态机
│       ├── dto/              # AI 数据传输对象
│       └── mapper/           # AI 数据访问
├── frontend-vue/
│   └── src/
│       ├── views/            # 首页、详情页
│       ├── components/       # AI 聊天、座位选择、购票表单等
│       └── router/           # 路由配置
└── pom.xml
```

## 技术要点

### Redis Lua 原子锁座

通过 Lua 脚本实现座位原子锁定，避免分布式锁开销和超卖问题：

```lua
-- 简化逻辑：检查 bitmap → 锁座 → 记录用户
local locked = redis.call('setbit', KEYS[1], ARGV[1], 1)
if locked == 0 then
    redis.call('hset', KEYS[2], ARGV[1], ARGV[2])
    return 1  -- 锁座成功
end
return 0  -- 座位已被锁
```

### RocketMQ 异步订单流程

```
用户请求 → Redis Lua 锁座 → 发送 MQ (异步) → 数据库落单
                              └→ 延迟 MQ (5秒) → 超时检查 → 自动释放
```

### AI 意图识别协议

LLM 响应中使用标记嵌入结构化 JSON（`__TICKET_INTENT__{...}__TICKET_INTENT_END__`），服务端解析为类型化 DTO，实现自然语言到购票行为的转换。

### 抽奖策略模式

`DrawStrategy` 接口定义抽奖行为，`DefaultDrawStrategy`、`LastDrawStrategy` 等实现不同算法，`DrawStrategyFactory` 根据活动配置动态选择策略，`AwardDispatchFactory` 处理实物/虚拟奖品分发。

## 待完善

- [ ] 核心服务单元测试（锁座、抽奖）
- [ ] Docker Compose 本地开发环境
- [ ] CI/CD 流水线
- [ ] WebSocket 实时座位状态推送
- [ ] 支付模块模拟

## License

MIT
