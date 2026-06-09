# 🎫 Damai Ticketing Platform

A high-concurrency ticketing platform built with Spring Boot & Vue.js, featuring distributed messaging, Redis-based seat locking, AI-powered assistant, and lottery system.

> **Demo project** — inspired by Damai ticketing system architecture.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| **Backend** | Java 17, Spring Boot 3.5, MyBatis, ShardingSphere |
| **Frontend** | Vue 3 (Composition API), Vite, Element Plus, Vue Router |
| **Messaging** | Apache RocketMQ (async order processing, delay queues) |
| **Cache & Lock** | Redis + Lua scripting for atomic seat locking |
| **AI** | Spring AI, OpenAI/DeepSeek LLM, DashScope ASR, MCP |
| **Database** | MySQL (sharded via ShardingSphere) |
| **Infra** | Nacos, HikariCP, Feign, Sentinel |

## Features

### Core Ticketing
- **Concurrent seat locking** — Redis Lua script ensures atomic lock/unlock under high contention
- **Async order flow** — RocketMQ decouples order creation, payment timeout handling via delay queues
- **Stock tracking** — Change-log streaming via MQ with audit trail

### AI Assistant
- **Natural language ticket search** — LLM-powered intent recognition (`__TICKET_INTENT__` protocol)
- **Speech recognition** — Alibaba DashScope ASR integration
- **Vector search** — Semantic retrieval over program/event data
- **State machine** — Chat state management for guided conversations
- **MCP tools** — Spring AI MCP client for external tool calling

### Lottery System
- Strategy pattern (`DrawStrategy`) with pluggable draw algorithms
- Default, last-prize, and weighted draw strategies
- Physical & virtual award dispatching

### Additional
- Bloom filter for cache optimization
- ShardingSphere for horizontal DB scaling
- Feign + Sentinel for service resilience

## Architecture

```
┌─────────────┐     ┌─────────────────────────────────────┐
│  Vue 3 SPA   │────▶│       Spring Boot Backend           │
│  (Vite)      │     │  ┌─────────┐  ┌──────────────────┐  │
└─────────────┘     │  │Controller│─▶│   Service Layer   │  │
                    │  └─────────┘  └───────┬──────────┘  │
                    │                       │              │
                    │  ┌────────────────────▼──────────┐   │
                    │  │   Redis (Lua lock + cache)    │   │
                    │  └───────────────────────────────┘   │
                    │  ┌───────────────────────────────┐   │
                    │  │   RocketMQ (async / delay)    │   │
                    │  └───────────────────────────────┘   │
                    │  ┌───────────────────────────────┐   │
                    │  │   MySQL (ShardingSphere)      │   │
                    │  └───────────────────────────────┘   │
                    │  ┌───────────────────────────────┐   │
                    │  │   Spring AI / LLM / MCP       │   │
                    │  └───────────────────────────────┘   │
                    └─────────────────────────────────────┘
```

## Quick Start

### Prerequisites

- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0+
- Redis 6+
- RocketMQ 5.x
- Nacos 2.x (optional)

### Configuration

Copy the template config and fill in your credentials:

```bash
cp src/main/resources/application-template.yaml src/main/resources/application.yaml
# Edit application.yaml with your database, Redis, and API keys
```

### Backend

```bash
mvn clean install -DskipTests
mvn spring-boot:run
```

Server starts at `http://localhost:6086`

### Frontend

```bash
cd frontend-vue
npm install
npm run dev
```

App runs at `http://localhost:5173`

### Services Required

| Service | Default |
|---------|---------|
| MySQL | localhost:3306 / database: demotest01 |
| Redis | localhost:6379 |
| RocketMQ | localhost:9876 |
| Nacos | localhost:8848 |

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/ticket/seat/create` | Create order & lock seat |
| GET | `/ticket/seat/release` | Release seat |
| POST | `/api/ai/chat` | AI chat (SSE stream) |
| POST | `/api/ai/chat/process` | AI chat with intent parsing |
| GET | `/api/ai/ping` | AI service health check |

## Project Structure

```
demo/
├── src/main/java/com/
│   ├── example/
│   │   ├── controller/       # REST controllers
│   │   ├── service/          # Business logic + RocketMQ consumers
│   │   ├── mapper/           # MyBatis data mappers
│   │   ├── entity/           # Domain entities
│   │   ├── dto/              # Data transfer objects
│   │   ├── DrawStrategy/     # Lottery strategy pattern
│   │   ├── mq/               # MQ callbacks
│   │   ├── constant/         # Constants
│   │   ├── core/             # Utilities (SpringUtil, etc.)
│   │   └── redis/            # Redis lock test
│   └── Ai/
│       ├── Controller/       # AI-related endpoints
│       ├── Service/          # AI service implementations
│       ├── config/           # Spring AI, MCP, CORS config
│       ├── state/            # Chat state machine
│       ├── dto/              # AI DTOs
│       └── mapper/           # AI data access
├── frontend-vue/
│   └── src/
│       ├── views/            # Home, Detail pages
│       ├── components/       # AI Chat, SeatSelector, TicketForm, etc.
│       └── router/           # Vue Router config
└── pom.xml
```

## Key Technical Highlights

### Redis Lua Seat Locking

Atomic seat locking via Lua script — prevents oversell without distributed lock overhead:

```lua
-- Simplified: checks bitmap, locks seat, records change log atomically
local locked = redis.call('setbit', KEYS[1], ARGV[1], 1)
if locked == 0 then
    redis.call('hset', KEYS[2], ARGV[1], ARGV[2])
    return 1  -- success
end
return 0  -- already locked
```

### RocketMQ Async Order Flow

```
Request → Redis Lua Lock → Send MQ (async) → DB insert
                            └→ Delay MQ (5s) → Timeout check → Auto-release
```

### AI Intent Protocol

LLM responses embed structured JSON between markers (`__TICKET_INTENT__{...}__TICKET_INTENT_END__`), parsed server-side into typed DTOs for downstream processing.

## Roadmap / TODOs

- [ ] Unit tests for core services (seat locking, lottery draw)
- [ ] Docker Compose for local dev environment
- [ ] CI/CD pipeline
- [ ] WebSocket for real-time seat status push
- [ ] Payment mock module

## License

MIT
