<template>
  <div class="state-machine-demo">
    <div class="demo-header">
      <h2>🤖 AI客服状态机学习Demo</h2>
      <p class="subtitle">通过交互式演示学习状态机的状态与转换</p>
    </div>

    <div class="demo-content">
      <!-- 左侧：状态图可视化 -->
      <div class="state-diagram">
        <h3>状态流转图</h3>
        <svg class="state-svg" viewBox="0 0 600 450">
          <!-- 连接线 -->
          <defs>
            <marker id="arrowhead" markerWidth="10" markerHeight="7" refX="9" refY="3.5" orient="auto">
              <polygon points="0 0, 10 3.5, 0 7" fill="#667eea"/>
            </marker>
          </defs>

          <!-- 状态节点 -->
          <g v-for="(state, key) in states" :key="key"
             :class="['state-node', { active: currentState === key, final: state.isFinal }]"
             @click="transition(state.transitions[0]?.event)">
            <circle :cx="state.x" :cy="state.y" r="45"/>
            <text :x="state.x" :y="state.y - 8" class="state-name">{{ state.name }}</text>
            <text :x="state.x" :y="state.y + 12" class="state-label">{{ state.label }}</text>
          </g>

          <!-- 连接线和标签 -->
          <g v-for="(state, key) in states" :key="'line-' + key">
            <line
              v-for="trans in state.transitions"
              :key="trans.event"
              :x1="state.x"
              :y1="state.y"
              :x2="states[trans.target].x"
              :y2="states[trans.target].y"
              class="transition-line"
            />
          </g>
        </svg>
      </div>

      <!-- 右侧：状态信息面板 -->
      <div class="info-panel">
        <div class="current-state-card">
          <h3>当前状态</h3>
          <div class="state-badge" :class="currentState">
            {{ states[currentState].name }}
          </div>
          <p class="state-desc">{{ states[currentState].description }}</p>
        </div>

        <div class="event-history">
          <h3>事件历史</h3>
          <div class="history-list">
            <div v-for="(event, index) in eventHistory" :key="index" class="history-item">
              <span class="event-name">{{ event.event }}</span>
              <span class="event-arrow">→</span>
              <span class="state-name">{{ event.toState }}</span>
            </div>
          </div>
        </div>

        <div class="available-events">
          <h3>可执行事件</h3>
          <div class="event-buttons">
            <button
              v-for="trans in states[currentState].transitions"
              :key="trans.event"
              class="event-btn"
              @click="triggerEvent(trans.event)"
            >
              {{ trans.event }}
            </button>
          </div>
        </div>

        <div class="state-table">
          <h3>状态转换表</h3>
          <table>
            <thead>
              <tr>
                <th>当前状态</th>
                <th>事件</th>
                <th>目标状态</th>
                <th>动作</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="(trans, key) in transitionTable"
                :key="key"
                :class="{ highlighted: trans.from === currentState }"
              >
                <td>{{ trans.from }}</td>
                <td>{{ trans.event }}</td>
                <td>{{ trans.to }}</td>
                <td>{{ trans.action }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- 底部：模拟对话演示 -->
    <div class="conversation-demo">
      <h3>💬 模拟对话演示</h3>
      <div class="sim-chat">
        <div
          v-for="(msg, index) in simMessages"
          :key="index"
          class="sim-message"
          :class="msg.role"
        >
          <div class="sim-avatar">{{ msg.role === 'user' ? '👤' : '🤖' }}</div>
          <div class="sim-bubble">
            <div class="sim-text">{{ msg.text }}</div>
            <div class="sim-state" v-if="msg.state">状态: {{ msg.state }}</div>
          </div>
        </div>
      </div>
      <div class="sim-controls">
        <button @click="runSimulation" :disabled="simRunning" class="sim-btn">
          {{ simRunning ? '模拟运行中...' : '▶️ 运行模拟' }}
        </button>
        <button @click="resetSimulation" class="sim-btn reset">🔄 重置</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'

// 状态机配置
const states = {
  IDLE: {
    name: 'IDLE',
    label: '空闲',
    description: '系统初始状态，等待用户发起会话',
    x: 100, y: 100,
    isFinal: false,
    transitions: [
      { event: 'USER_CONNECT', target: 'GREETING', action: '发送欢迎语' }
    ]
  },
  GREETING: {
    name: 'GREETING',
    label: '问候中',
    description: 'AI正在发送欢迎语和提示，等待用户输入',
    x: 300, y: 100,
    isFinal: false,
    transitions: [
      { event: 'USER_MESSAGE', target: 'PROCESSING', action: '接收并解析用户消息' }
    ]
  },
  PROCESSING: {
    name: 'PROCESSING',
    label: '处理中',
    description: 'AI正在理解用户问题，调用LLM分析意图',
    x: 500, y: 100,
    isFinal: false,
    transitions: [
      { event: 'INTENT_RECOGNIZED', target: 'RESPONDING', action: '生成回复内容' },
      { event: 'ESCALATE', target: 'ESCALATING', action: '转接人工客服' }
    ]
  },
  RESPONDING: {
    name: 'RESPONDING',
    label: '回复中',
    description: 'AI正在回复用户，等待用户反馈',
    x: 500, y: 250,
    isFinal: false,
    transitions: [
      { event: 'USER_SATISFIED', target: 'RESOLVED', action: '标记为已解决' },
      { event: 'USER_DISSATISFIED', target: 'PROCESSING', action: '重新处理' },
      { event: 'NEED_MORE_INFO', target: 'WAITING_INPUT', action: '请求更多信息' }
    ]
  },
  WAITING_INPUT: {
    name: 'WAITING_INPUT',
    label: '等待输入',
    description: '等待用户补充信息或回答追问',
    x: 300, y: 250,
    isFinal: false,
    transitions: [
      { event: 'USER_MESSAGE', target: 'PROCESSING', action: '继续处理' }
    ]
  },
  ESCALATING: {
    name: 'ESCALATING',
    label: '转人工',
    description: '正在转接人工客服，会话由人工接管',
    x: 500, y: 400,
    isFinal: false,
    transitions: [
      { event: 'AGENT接手', target: 'HUMAN_SUPPORT', action: '人工客服接管' }
    ]
  },
  HUMAN_SUPPORT: {
    name: 'HUMAN_SUPPORT',
    label: '人工服务',
    description: '人工客服正在服务',
    x: 300, y: 400,
    isFinal: false,
    transitions: [
      { event: 'RESOLVED', target: 'CLOSED', action: '人工关闭会话' }
    ]
  },
  RESOLVED: {
    name: 'RESOLVED',
    label: '已解决',
    description: '问题已解决，等待用户确认或结束会话',
    x: 100, y: 250,
    isFinal: false,
    transitions: [
      { event: 'CLOSE', target: 'CLOSED', action: '关闭会话' },
      { event: 'NEW_ISSUE', target: 'PROCESSING', action: '处理新问题' }
    ]
  },
  CLOSED: {
    name: 'CLOSED',
    label: '已关闭',
    description: '会话已结束',
    x: 100, y: 400,
    isFinal: true,
    transitions: []
  }
}

// 转换表数据
const transitionTable = [
  { from: 'IDLE', event: 'USER_CONNECT', to: 'GREETING', action: '发送欢迎语' },
  { from: 'GREETING', event: 'USER_MESSAGE', to: 'PROCESSING', action: '解析用户消息' },
  { from: 'PROCESSING', event: 'INTENT_RECOGNIZED', to: 'RESPONDING', action: '生成回复' },
  { from: 'PROCESSING', event: 'ESCALATE', to: 'ESCALATING', action: '转人工' },
  { from: 'RESPONDING', event: 'USER_SATISFIED', to: 'RESOLVED', action: '标记已解决' },
  { from: 'RESPONDING', event: 'USER_DISSATISFIED', to: 'PROCESSING', action: '重新处理' },
  { from: 'RESPONDING', event: 'NEED_MORE_INFO', to: 'WAITING_INPUT', action: '请求信息' },
  { from: 'WAITING_INPUT', event: 'USER_MESSAGE', to: 'PROCESSING', action: '继续处理' },
  { from: 'ESCALATING', event: 'AGENT接手', to: 'HUMAN_SUPPORT', action: '人工接管' },
  { from: 'HUMAN_SUPPORT', event: 'RESOLVED', to: 'CLOSED', action: '关闭会话' },
  { from: 'RESOLVED', event: 'CLOSE', to: 'CLOSED', action: '结束会话' },
  { from: 'RESOLVED', event: 'NEW_ISSUE', to: 'PROCESSING', action: '新问题' }
]

// 当前状态
const currentState = ref('IDLE')

// 事件历史
const eventHistory = ref([])

// 触发事件
function triggerEvent(eventName) {
  const state = states[currentState.value]
  const transition = state.transitions.find(t => t.event === eventName)

  if (!transition) {
    console.warn(`无效事件: ${eventName}，当前状态: ${currentState.value}`)
    return
  }

  eventHistory.value.unshift({
    event: eventName,
    fromState: currentState.value,
    toState: transition.target,
    action: transition.action
  })

  currentState.value = transition.target
}

// 模拟对话相关
const simMessages = ref([
  { role: 'bot', text: '您好！我是AI客服小助，请问有什么可以帮您？', state: 'GREETING' }
])
const simRunning = ref(false)

async function runSimulation() {
  simRunning.value = true
  simMessages.value = [{ role: 'bot', text: '您好！我是AI客服小助，请问有什么可以帮您？', state: 'GREETING' }]
  currentState.value = 'IDLE'
  eventHistory.value = []

  const scenario = [
    { delay: 1000, event: 'USER_CONNECT', state: 'GREETING' },
    { delay: 1500, msg: '我想查询我的订单', role: 'user' },
    { delay: 500, event: 'USER_MESSAGE', state: 'PROCESSING' },
    { delay: 2000, msg: '正在为您查询订单信息...', role: 'bot' },
    { delay: 1000, event: 'INTENT_RECOGNIZED', state: 'RESPONDING' },
    { delay: 2000, msg: '您的订单号是 #12345，预计明天送达。请问还有其他问题吗？', role: 'bot' },
    { delay: 1500, msg: '没有了，谢谢！', role: 'user' },
    { delay: 1000, event: 'USER_SATISFIED', state: 'RESOLVED' },
    { delay: 1500, msg: '好的，感谢您的咨询，祝您生活愉快！👋', role: 'bot' },
    { delay: 1000, event: 'CLOSE', state: 'CLOSED' }
  ]

  for (const step of scenario) {
    await new Promise(resolve => setTimeout(resolve, step.delay))
    if (step.event) {
      triggerEvent(step.event)
    }
    if (step.msg) {
      simMessages.value.push({ role: step.role, text: step.msg, state: currentState.value })
    }
  }

  simRunning.value = false
}

function resetSimulation() {
  simMessages.value = [{ role: 'bot', text: '您好！我是AI客服小助，请问有什么可以帮您？', state: 'GREETING' }]
  currentState.value = 'IDLE'
  eventHistory.value = []
  simRunning.value = false
}
</script>

<style scoped>
.state-machine-demo {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
  font-family: 'Segoe UI', sans-serif;
}

.demo-header {
  text-align: center;
  margin-bottom: 30px;
}

.demo-header h2 {
  color: #1a1a2e;
  margin: 0 0 10px;
}

.subtitle {
  color: #666;
  margin: 0;
}

.demo-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
  margin-bottom: 30px;
}

/* 状态图样式 */
.state-diagram {
  background: #f8fafc;
  border-radius: 16px;
  padding: 20px;
}

.state-diagram h3 {
  margin: 0 0 15px;
  color: #334155;
}

.state-svg {
  width: 100%;
  height: 450px;
}

.state-node {
  cursor: pointer;
  transition: all 0.3s;
}

.state-node circle {
  fill: #e2e8f0;
  stroke: #94a3b8;
  stroke-width: 2;
  transition: all 0.3s;
}

.state-node.active circle {
  fill: #667eea;
  stroke: #5a67d8;
  stroke-width: 3;
}

.state-node.final circle {
  fill: #10b981;
  stroke: #059669;
  stroke-width: 2;
}

.state-node:hover circle {
  fill: #818cf8;
  transform: scale(1.05);
}

.state-node text {
  fill: #1e293b;
  font-size: 12px;
  text-anchor: middle;
  pointer-events: none;
}

.state-node.active text {
  fill: #fff;
  font-weight: bold;
}

.state-name {
  font-size: 11px;
  font-weight: bold;
}

.state-label {
  font-size: 10px;
  fill: #64748b;
}

.state-node.active .state-label {
  fill: #e0e7ff;
}

.transition-line {
  stroke: #667eea;
  stroke-width: 2;
  stroke-dasharray: 5 3;
  marker-end: url(#arrowhead);
  opacity: 0.6;
}

/* 信息面板 */
.info-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-panel h3 {
  margin: 0 0 10px;
  color: #334155;
  font-size: 14px;
}

.current-state-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 20px;
  color: white;
}

.current-state-card h3 {
  color: rgba(255,255,255,0.8);
  font-size: 12px;
  margin-bottom: 10px;
}

.state-badge {
  display: inline-block;
  padding: 8px 16px;
  background: rgba(255,255,255,0.2);
  border-radius: 20px;
  font-weight: bold;
  font-size: 18px;
  margin-bottom: 10px;
}

.state-desc {
  margin: 0;
  font-size: 13px;
  opacity: 0.9;
}

.event-history, .available-events {
  background: white;
  border-radius: 12px;
  padding: 15px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.history-list {
  max-height: 120px;
  overflow-y: auto;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 0;
  border-bottom: 1px solid #f1f5f9;
  font-size: 13px;
}

.history-item:last-child {
  border-bottom: none;
}

.event-name {
  color: #667eea;
  font-weight: 500;
}

.event-arrow {
  color: #94a3b8;
}

.history-item .state-name {
  color: #10b981;
}

.event-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.event-btn {
  padding: 8px 16px;
  background: #f1f5f9;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  color: #475569;
  transition: all 0.2s;
}

.event-btn:hover {
  background: #667eea;
  color: white;
}

/* 状态表 */
.state-table {
  background: white;
  border-radius: 12px;
  padding: 15px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.state-table table {
  width: 100%;
  border-collapse: collapse;
  font-size: 12px;
}

.state-table th, .state-table td {
  padding: 8px;
  text-align: left;
  border-bottom: 1px solid #f1f5f9;
}

.state-table th {
  color: #64748b;
  font-weight: 500;
}

.state-table tr.highlighted {
  background: #f5f3ff;
}

.state-table td:first-child {
  color: #667eea;
  font-weight: 500;
}

.state-table td:nth-child(2) {
  color: #10b981;
}

.state-table td:last-child {
  color: #64748b;
  font-size: 11px;
}

/* 模拟对话 */
.conversation-demo {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.conversation-demo h3 {
  margin: 0 0 15px;
  color: #334155;
}

.sim-chat {
  background: #f8fafc;
  border-radius: 12px;
  padding: 15px;
  max-height: 250px;
  overflow-y: auto;
  margin-bottom: 15px;
}

.sim-message {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}

.sim-message.user {
  flex-direction: row-reverse;
}

.sim-avatar {
  width: 36px;
  height: 36px;
  background: #e2e8f0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.sim-message.bot .sim-avatar {
  background: #ddd6fe;
}

.sim-bubble {
  max-width: 70%;
}

.sim-text {
  padding: 10px 14px;
  border-radius: 12px;
  background: white;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  font-size: 14px;
  line-height: 1.5;
}

.sim-message.user .sim-text {
  background: #667eea;
  color: white;
}

.sim-state {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 4px;
  padding-left: 4px;
}

.sim-controls {
  display: flex;
  gap: 10px;
}

.sim-btn {
  padding: 10px 20px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.sim-btn:hover:not(:disabled) {
  background: #5a67d8;
}

.sim-btn:disabled {
  background: #94a3b8;
  cursor: not-allowed;
}

.sim-btn.reset {
  background: #64748b;
}

.sim-btn.reset:hover {
  background: #475569;
}

@media (max-width: 900px) {
  .demo-content {
    grid-template-columns: 1fr;
  }
}
</style>
