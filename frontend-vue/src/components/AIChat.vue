<template>
  <div class="ai-chat-shell">
    <ConversationList
        :conversations="conversations"
        :activeId="activeConversationId"
        @select="selectConversation"
        @new="createConversation"
        @delete="deleteConversation"
        :visible="sidebarVisible"
        @close="sidebarVisible = false"
    />

    <div class="ai-chat" :class="{ 'with-sidebar': sidebarVisible }">
      <header class="ai-chat__header">
        <button class="toggle-sidebar" @click="toggleSidebar" aria-label="Toggle conversations">
          ☰
        </button>
        <div class="chat-header-info">
          <h2>{{ currentConversation?.title || 'AI 客服' }}</h2>
          <small v-if="currentConversation?.lastSummary">{{ currentConversation.lastSummary }}</small>
        </div>
        <div class="header-actions">
          <button class="btn-clear" @click="clearCurrentConversation">清空会话</button>
          <button class="btn-new" @click="createConversation">新会话</button>
        </div>
      </header>

      <main class="ai-chat__body" ref="bodyRef">
        <div v-if="currentMessages.length === 0" class="ai-chat__empty">
          这是空会话，发送第一条消息开始对话。
        </div>

        <div class="ai-chat__messages">
          <ChatMessage
              v-for="msg in currentMessages"
              :key="msg.id"
              :role="msg.role"
              :text="msg.text"
              :time="msg.time"
          />
          <div v-if="isTyping" class="ai-chat__typing">AI 正在输入…</div>
        </div>
      </main>

      <footer class="ai-chat__footer">
        <textarea
            v-model="input"
            :placeholder="placeholder"
            @keydown.enter.prevent="handleSendByEnter"
            rows="1"
            class="ai-chat__input"
        />
        <div class="ai-chat__controls">
          <button @click="toggleVoice" :disabled="voiceLoading" class="btn-voice" :class="{ recording: isRecording }">
            {{ voiceLoading ? '⏳' : (isRecording ? '⏹️' : '🎤') }}
          </button>
          <button @click="openTicketDialog" class="btn-ticket">
            🎫 购票
          </button>
          <button @click="send" :disabled="sending || inputTrimmed === ''" class="btn-send">
            {{ sending ? '发送中...' : '发送' }}
          </button>
        </div>
      </footer>
    </div>

    <!-- 原有的工具确认弹窗（保留通用工具调用） -->
    <div v-if="showConfirmDialog" class="confirm-overlay" @click.self="cancelToolCall">
      <div class="confirm-dialog">
        <div class="confirm-header">⚠️ 操作确认</div>
        <div class="confirm-body">
          <p>{{ confirmDialog.message }}</p>
          <div class="confirm-params">
            <div v-for="(value, key) in confirmDialog.params" :key="key" class="param-item">
              <span class="param-label">{{ key }}:</span>
              <span class="param-value">{{ value }}</span>
            </div>
          </div>
        </div>
        <div class="confirm-footer">
          <button class="btn-cancel" @click="cancelToolCall">取消</button>
          <button class="btn-confirm" @click="confirmToolCall">确认执行</button>
        </div>
      </div>
    </div>

    <!-- 购票确认弹窗 -->
    <TicketConfirmDialog
        :visible="showTicketDialog"
        :ticketInfo="ticketConfirmInfo"
        @confirm="handleTicketConfirm"
        @cancel="handleTicketCancel"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, nextTick, watch } from 'vue';
import ChatMessage from './ChatMessage.vue';
import ConversationList from './ConversationList.vue';
import TicketConfirmDialog from './TicketConfirmDialog.vue';
import { sendMessageToAi } from '../chatApi.js';

const mockMode = false;
const placeholder = '请在这里输入问题，例如："我想退票，流程是怎样的？"';
const API_BASE = 'http://localhost:6086/api/asr';
const AI_API_BASE = 'http://localhost:6086/api/ai';

const sidebarVisible = ref(false);
const input = ref('');
const sending = ref(false);
const isTyping = ref(false);
const bodyRef = ref(null);
const isRecording = ref(false);
const voiceLoading = ref(false);
let pollInterval = null;

const showConfirmDialog = ref(false);
const confirmDialog = ref({
  message: '',
  params: {},
  toolName: '',
  chatId: ''
});

// 购票确认相关
const showTicketDialog = ref(false);
const ticketConfirmInfo = ref({});

const STORAGE_KEY = 'ai_chat_conversations_v1';
const persisted = localStorage.getItem(STORAGE_KEY);
const conversations = reactive(persisted ? JSON.parse(persisted) : []);

if (conversations.length === 0) {
  conversations.push({
    id: genId(),
    title: '新的会话',
    createdAt: Date.now(),
    lastSummary: '',
    messages: []
  });
}

const activeConversationId = ref(conversations[0].id);

const currentConversation = computed(() =>
    conversations.find(c => c.id === activeConversationId.value)
);
const currentMessages = computed(() => (currentConversation.value?.messages || []));

watch(
    () => conversations,
    (val) => {
      try {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(val));
      } catch (e) {
        console.warn('保存会话到 localStorage 失败', e);
      }
    },
    { deep: true }
);

function genId() {
  return `${Date.now()}-${Math.random().toString(36).slice(2, 7)}`;
}
const inputTrimmed = computed(() => input.value.trim());

function ensureActive() {
  if (!activeConversationId.value && conversations.length) {
    activeConversationId.value = conversations[0].id;
  }
}

function addMessageToCurrent(role, text) {
  ensureActive();
  const conv = conversations.find(c => c.id === activeConversationId.value);
  if (!conv) return;
  conv.messages.push({
    id: genId(),
    role,
    text,
    time: new Date().toLocaleTimeString()
  });
  conv.lastSummary = text.length > 40 ? text.slice(0, 40) + '...' : text;
  nextTick(() => {
    if (bodyRef.value) bodyRef.value.scrollTop = bodyRef.value.scrollHeight;
  });
}

async function send() {
  if (inputTrimmed.value === '' || sending.value) return;

  const userText = input.value;
  addMessageToCurrent('user', userText);
  input.value = '';
  sending.value = true;
  isTyping.value = true;

  ensureActive();
  const conv = conversations.find(c => c.id === activeConversationId.value);
  if (!conv) {
    sending.value = false;
    isTyping.value = false;
    return;
  }

  const botMessage = {
    id: genId(),
    role: 'bot',
    text: '',
    time: new Date().toLocaleTimeString()
  };
  conv.messages.push(botMessage);
  scrollToBottom();

  try {
    const res = await sendMessageToAi(userText, { sessionId: activeConversationId.value });
    
    // 获取回复文本
    let replyText = '';
    if (res && res.text) {
      replyText = res.text;
    } else if (typeof res === 'string') {
      replyText = res;
    } else if (res && typeof res === 'object') {
      replyText = JSON.stringify(res);
    }

    // 检测购票意图并显示确认弹窗
    const hasTicketIntent = parseTicketIntent(replyText);
    
    if (hasTicketIntent) {
      // 购票确认场景：显示弹窗，聊天框显示简短提示
      botMessage.text = '🎫 已为您生成购票信息，请确认以下内容是否正确';
      conv.lastSummary = '购票确认';
    } else {
      // 普通对话回复
      botMessage.text = replyText;
      conv.lastSummary = replyText.length > 40 ? replyText.slice(0, 40) + '...' : replyText;
    }
  } catch (err) {
    botMessage.text += '\n\n（call 失败，请检查后端或控制台日志）';
  } finally {
    sending.value = false;
    isTyping.value = false;
    sidebarVisible.value = false;
    scrollToBottom();
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (bodyRef.value) bodyRef.value.scrollTop = bodyRef.value.scrollHeight;
  });
}

function handleSendByEnter() {
  send();
}

function createConversation() {
  const id = genId();
  const newConv = {
    id,
    title: '新的会话',
    createdAt: Date.now(),
    lastSummary: '',
    messages: []
  };
  conversations.unshift(newConv);
  activeConversationId.value = id;
  sidebarVisible.value = false;
}

function selectConversation(id) {
  activeConversationId.value = id;
  sidebarVisible.value = false;
}

function deleteConversation(id) {
  const idx = conversations.findIndex(c => c.id === id);
  if (idx >= 0) {
    conversations.splice(idx, 1);
    if (conversations.length > 0) {
      activeConversationId.value = conversations[0].id;
    } else {
      createConversation();
    }
  }
}

function clearCurrentConversation() {
  const conv = conversations.find(c => c.id === activeConversationId.value);
  if (conv) {
    conv.messages = [];
    conv.lastSummary = '';
  }
}

function toggleSidebar() {
  sidebarVisible.value = !sidebarVisible.value;
}

// 语音相关
async function toggleVoice() {
  if (isRecording.value) {
    await stopVoiceRecording();
  } else {
    await startVoiceRecording();
  }
}

async function startVoiceRecording() {
  if (isRecording.value) return;
  isRecording.value = true;
  voiceLoading.value = true;

  try {
    const response = await fetch(`${API_BASE}/start`, { method: 'POST' });
    const data = await response.json();
    if (data.success) {
      startPolling();
    }
  } catch (error) {
    console.error('Start recording error:', error);
    isRecording.value = false;
  } finally {
    voiceLoading.value = false;
  }
}

async function stopVoiceRecording() {
  if (!isRecording.value) return;
  isRecording.value = true;
  stopPolling();

  try {
    const stopResponse = await fetch(`${API_BASE}/stop`, { method: 'POST' });
    const stopData = await stopResponse.json();

    if (stopData.success && stopData.text) {
      input.value = stopData.text;
      await send();
    }
  } catch (error) {
    console.error('Stop recording error:', error);
  } finally {
    isRecording.value = false;
  }
}

function startPolling() {
  pollInterval = setInterval(async () => {
    try {
      const response = await fetch(`${API_BASE}/status`);
      const data = await response.json();
      if (data.success && data.lastText) {
        input.value = data.lastText;
      }
    } catch (error) {
      console.error('Polling error:', error);
    }
  }, 300);
}

function stopPolling() {
  if (pollInterval) {
    clearInterval(pollInterval);
    pollInterval = null;
  }
}

// 工具调用确认
async function confirmToolCall() {
  showConfirmDialog.value = false;
  sending.value = true;
  isTyping.value = true;

  ensureActive();
  const conv = conversations.find(c => c.id === activeConversationId.value);
  if (!conv) {
    sending.value = false;
    isTyping.value = false;
    return;
  }

  const botMessage = {
    id: genId(),
    role: 'bot',
    text: '正在执行...',
    time: new Date().toLocaleTimeString()
  };
  conv.messages.push(botMessage);
  scrollToBottom();

  try {
    const res = await fetch(`${AI_API_BASE}/execute`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        toolName: confirmDialog.value.toolName,
        params: confirmDialog.value.params
      })
    });
    const data = await res.json();

    if (data.success) {
      botMessage.text = data.message;
    } else {
      botMessage.text = '执行失败: ' + data.error;
    }
  } catch (err) {
    botMessage.text = '执行失败: ' + err.message;
  } finally {
    sending.value = false;
    isTyping.value = false;
    scrollToBottom();
  }
}

function cancelToolCall() {
  showConfirmDialog.value = false;
  sending.value = false;
  isTyping.value = false;
}

// 购票确认处理
function handleTicketConfirm(ticketInfo) {
  showTicketDialog.value = false;
  sending.value = true;
  isTyping.value = true;

  ensureActive();
  const conv = conversations.find(c => c.id === activeConversationId.value);
  if (!conv) {
    sending.value = false;
    isTyping.value = false;
    return;
  }

  const botMessage = {
    id: genId(),
    role: 'bot',
    text: '正在执行购票...',
    time: new Date().toLocaleTimeString()
  };
  conv.messages.push(botMessage);
  scrollToBottom();

  fetch(`${AI_API_BASE}/execute`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      toolName: 'createTicketOrder',
      params: ticketInfo
    })
  }).then(res => res.json())
    .then(data => {
      if (data.success) {
        botMessage.text = '✅ 购票成功！\n' + data.message;
      } else {
        botMessage.text = '❌ 购票失败: ' + data.error;
      }
    })
    .catch(err => {
      botMessage.text = '❌ 购票异常: ' + err.message;
    })
    .finally(() => {
      sending.value = false;
      isTyping.value = false;
      scrollToBottom();
    });
}

function handleTicketCancel() {
  showTicketDialog.value = false;
}

// 手动打开购票确认弹窗
function openTicketDialog() {
  ticketConfirmInfo.value = {};
  showTicketDialog.value = true;
}

// 解析LLM返回的购票意图并显示确认弹窗
function parseTicketIntent(responseText) {
  // 方式1：检查是否包含 __TICKET_INTENT__ 标记
  if (responseText.includes('__TICKET_INTENT__')) {
    try {
      const jsonMatch = responseText.match(/__TICKET_INTENT__([\s\S]*?)__TICKET_INTENT_END__/);
      if (jsonMatch) {
        const ticketData = JSON.parse(jsonMatch[1]);
        ticketConfirmInfo.value = ticketData;
        showTicketDialog.value = true;
        return true;
      }
    } catch (e) {
      console.error('解析 __TICKET_INTENT__ 失败', e);
    }
  }

  // 方式2：从 markdown 表格中解析购票信息
  const ticketInfo = parseMarkdownTable(responseText);
  if (ticketInfo) {
    ticketConfirmInfo.value = ticketInfo;
    showTicketDialog.value = true;
    return true;
  }

  return false;
}

// 从 markdown 表格解析购票信息
function parseMarkdownTable(text) {
  // 检查是否包含购票相关的 markdown 表格
  if (!text.includes('|') || !text.includes('🎫')) {
    return null;
  }

  try {
    const lines = text.split('\n');
    const tableData = {};

    for (const line of lines) {
      if (!line.includes('|')) continue;

      const cells = line.split('|').map(c => c.trim()).filter(c => c);
      if (cells.length < 2) continue;

      // 提取 key-value
      const key = cells[0].replace(/\*\*/g, '').replace(/🎫|🎤|🎟️|👤/g, '').trim();
      const value = cells[1].replace(/\*\*/g, '').trim();

      // 匹配常见购票字段
      if (key.includes('演出') || key.includes('节目')) {
        tableData.eventName = value;
      } else if (key.includes('时间') || key.includes('日期')) {
        tableData.showTime = value;
      } else if (key.includes('场馆') || key.includes('地点')) {
        tableData.venue = value;
      } else if (key.includes('票价') || key.includes('价格') || key.includes('金额')) {
        const priceMatch = value.match(/[\d,]+/);
        if (priceMatch) {
          tableData.price = parseInt(priceMatch[0].replace(/,/g, ''));
        }
      } else if (key.includes('座位')) {
        // 解析座位信息，如 "A区3排15号"
        const seatMatch = value.match(/([A-Z])区(\d+)排(\d+)号?/);
        if (seatMatch) {
          tableData.seats = [{
            area: seatMatch[1],
            row: seatMatch[2],
            seat: seatMatch[3]
          }];
        }
      } else if (key.includes('购票人') || key.includes('姓名')) {
        if (!tableData.buyerInfo) tableData.buyerInfo = {};
        tableData.buyerInfo.name = value;
      } else if (key.includes('电话') || key.includes('手机')) {
        if (!tableData.buyerInfo) tableData.buyerInfo = {};
        tableData.buyerInfo.phone = value;
      } else if (key.includes('证件') || key.includes('身份证')) {
        if (!tableData.buyerInfo) tableData.buyerInfo = {};
        tableData.buyerInfo.idCard = value;
      } else if (key.includes('数量')) {
        tableData.ticketCount = parseInt(value) || 1;
      }
    }

    // 只有包含演出名称或座位信息才算是购票确认
    if (tableData.eventName || (tableData.seats && tableData.seats.length > 0)) {
      return tableData;
    }
  } catch (e) {
    console.error('解析 markdown 表格失败', e);
  }

  return null;
}

watch(currentMessages, () => {
  nextTick(() => {
    if (bodyRef.value) bodyRef.value.scrollTop = bodyRef.value.scrollHeight;
  });
});
</script>

<style scoped>
.ai-chat-shell {
  display: flex;
  height: 100vh;
  width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  overflow: hidden;
}

.ai-chat {
  display: flex;
  flex-direction: column;
  flex: 1;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-left: 1px solid rgba(255, 255, 255, 0.2);
  min-width: 0;
  max-width: 100%;
}

.ai-chat__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: linear-gradient(90deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  border-bottom: 1px solid rgba(102, 126, 234, 0.15);
  gap: 12px;
}

.toggle-sidebar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  border: 1px solid rgba(102, 126, 234, 0.2);
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.toggle-sidebar:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.chat-header-info {
  flex: 1;
  min-width: 0;
}

.chat-header-info h2 {
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
  margin: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.chat-header-info small {
  font-size: 11px;
  color: #718096;
  display: block;
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.header-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.ai-chat__body {
  flex: 1;
  overflow: auto;
  padding: 12px 16px;
  background: linear-gradient(180deg, #f7fafc 0%, #edf2f7 100%);
  scroll-behavior: smooth;
  max-height: calc(100vh - 140px);
}

.ai-chat__messages {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ai-chat__typing {
  color: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  font-size: 13px;
  font-weight: 500;
  padding: 8px 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.ai-chat__typing::before {
  content: '';
  width: 8px;
  height: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  animation: typing-bounce 1.4s infinite ease-in-out;
}

@keyframes typing-bounce {
  0%, 80%, 100% { transform: scale(0); opacity: 0.5; }
  40% { transform: scale(1); opacity: 1; }
}

.ai-chat__empty {
  color: #a0aec0;
  text-align: center;
  margin-top: 60px;
  font-size: 14px;
  padding: 24px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 16px;
  border: 1px dashed rgba(102, 126, 234, 0.2);
}

.ai-chat__footer {
  display: flex;
  gap: 10px;
  align-items: flex-end;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.98);
  border-top: 1px solid rgba(102, 126, 234, 0.1);
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.05);
}

.ai-chat__input {
  flex: 1;
  resize: none;
  padding: 12px 16px;
  border-radius: 12px;
  border: 2px solid rgba(102, 126, 234, 0.15);
  background: rgba(247, 250, 252, 0.8);
  font-size: 14px;
  color: #2d3748;
  transition: all 0.3s ease;
  min-height: 44px;
  max-height: 120px;
}

.ai-chat__input:focus {
  outline: none;
  border-color: rgba(102, 126, 234, 0.5);
  background: #fff;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

.ai-chat__input::placeholder {
  color: #a0aec0;
}

.ai-chat__controls {
  display: flex;
  flex-direction: row;
  gap: 8px;
  align-items: center;
}

.btn-ticket {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a5a 100%);
  color: white;
  border: none;
  padding: 10px 16px;
  border-radius: 10px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(255, 107, 107, 0.3);
}

.btn-ticket:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.4);
}

.btn-send {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 10px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.btn-send:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-send:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.btn-voice {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  border: none;
  padding: 10px 14px;
  border-radius: 10px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(245, 87, 108, 0.3);
}

.btn-voice:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(245, 87, 108, 0.4);
}

.btn-voice.recording {
  background: linear-gradient(135deg, #ff4757 0%, #ff6b81 100%);
  animation: pulse-voice 1s infinite;
}

@keyframes pulse-voice {
  0% { box-shadow: 0 0 0 0 rgba(255, 71, 87, 0.6); }
  70% { box-shadow: 0 0 0 10px rgba(255, 71, 87, 0); }
  100% { box-shadow: 0 0 0 0 rgba(255, 71, 87, 0); }
}

.btn-clear, .btn-new {
  padding: 8px 14px;
  border: 1px solid rgba(102, 126, 234, 0.2);
  background: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  color: #667eea;
  font-weight: 500;
  transition: all 0.3s ease;
}

.btn-clear:hover, .btn-new:hover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-color: transparent;
}

@media (min-width: 900px) {
  .ai-chat-shell > *:first-child {
    transform: none !important;
    position: relative;
  }
}

.confirm-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.confirm-dialog {
  background: white;
  border-radius: 16px;
  width: 400px;
  max-width: 90%;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  overflow: hidden;
}

.confirm-header {
  padding: 16px 20px;
  background: linear-gradient(135deg, #ff9800 0%, #ffb74d 100%);
  color: white;
  font-weight: bold;
  font-size: 16px;
}

.confirm-body {
  padding: 20px;
}

.confirm-body p {
  margin-bottom: 15px;
  color: #333;
}

.confirm-params {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 12px;
}

.param-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #eee;
}

.param-item:last-child {
  border-bottom: none;
}

.param-label {
  color: #666;
  font-weight: 600;
}

.param-value {
  color: #667eea;
  font-weight: 500;
}

.confirm-footer {
  padding: 15px 20px;
  border-top: 1px solid #eee;
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  background: #fafafa;
}

.btn-cancel {
  padding: 10px 20px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
}

.btn-cancel:hover {
  background: #f5f5f5;
}

.btn-confirm {
  padding: 10px 20px;
  border: none;
  background: linear-gradient(135deg, #4caf50 0%, #66bb6a 100%);
  color: white;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(76, 175, 80, 0.3);
}

.btn-confirm:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.4);
}
</style>