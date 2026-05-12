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
          <button @click="send" :disabled="sending || inputTrimmed === ''" class="btn-send">
            {{ sending ? '发送中...' : '发送' }}
          </button>
        </div>
      </footer>
    </div>

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
  </div>
</template>

<script setup>
import { ref, reactive, computed, nextTick, watch } from 'vue';
import ChatMessage from './ChatMessage.vue';
import ConversationList from './ConversationList.vue';
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
    const reply = (res && res.text) ? res.text : (typeof res === 'string' ? res : JSON.stringify(res));
    botMessage.text += reply;
    conv.lastSummary = botMessage.text.length > 40 ? botMessage.text.slice(0, 40) + '...' : botMessage.text;
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
  background: #f5f7fa;
  overflow: hidden;
}

.ai-chat {
  display: flex;
  flex-direction: column;
  flex: 1;
  background: #fff;
  border-left: 1px solid #e6e6e6;
  min-width: 0;
}

.ai-chat__header {
  display:flex;
  align-items:center;
  justify-content:space-between;
  padding: 10px 12px;
  border-bottom: 1px solid #f0f0f0;
  gap: 12px;
}
.toggle-sidebar {
  display:inline-flex;
  align-items:center;
  justify-content:center;
  width:36px;
  height:36px;
  border-radius:6px;
  border:1px solid #eee;
  background:#fff;
  cursor:pointer;
}
.chat-header-info {
  flex:1;
  min-width:0;
}
.header-actions {
  display:flex;
  gap:8px;
  align-items:center;
}

.ai-chat__body {
  flex: 1;
  overflow: auto;
  padding: 16px;
  background: #fafafa;
}
.ai-chat__messages {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.ai-chat__typing {
  color: #888;
  font-size: 13px;
}
.ai-chat__empty {
  color:#888;
  text-align:center;
  margin-top:20px;
}

.ai-chat__footer {
  display:flex;
  gap:10px;
  align-items:center;
  padding: 10px;
  border-top: 1px solid #f0f0f0;
}
.ai-chat__input {
  flex: 1;
  resize: none;
  padding: 10px;
  border-radius: 6px;
  border: 1px solid #ddd;
  min-height: 40px;
}
.ai-chat__controls {
  display:flex;
  flex-direction:column;
  gap:6px;
}
.btn-send {
  background:#409EFF;
  color:white;
  border:none;
  padding:8px 12px;
  border-radius:6px;
  cursor:pointer;
}
.btn-send:disabled {
  opacity:0.6;
  cursor: not-allowed;
}
.btn-voice {
  background:#7b2ff7;
  color:white;
  border:none;
  padding:8px 12px;
  border-radius:6px;
  cursor:pointer;
  font-size:16px;
  transition: all 0.3s;
}
.btn-voice:hover:not(:disabled) {
  background:#6b1fe6;
}
.btn-voice.recording {
  background:#ff4757;
  animation: pulse-voice 1s infinite;
}
@keyframes pulse-voice {
  0% { box-shadow: 0 0 0 0 rgba(255,71,87,0.7); }
  70% { box-shadow: 0 0 0 8px rgba(255,71,87,0); }
  100% { box-shadow: 0 0 0 0 rgba(255,71,87,0); }
}
.btn-clear, .btn-new {
  padding:6px 10px;
  border:1px solid #ddd;
  background:#fff;
  border-radius:6px;
  cursor:pointer;
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
  background: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}
.confirm-dialog {
  background: white;
  border-radius: 12px;
  width: 400px;
  max-width: 90%;
  box-shadow: 0 10px 40px rgba(0,0,0,0.3);
}
.confirm-header {
  padding: 16px 20px;
  background: #ff9800;
  color: white;
  font-weight: bold;
  font-size: 16px;
  border-radius: 12px 12px 0 0;
}
.confirm-body {
  padding: 20px;
}
.confirm-body p {
  margin-bottom: 15px;
  color: #333;
}
.confirm-params {
  background: #f5f5f5;
  border-radius: 8px;
  padding: 12px;
}
.param-item {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
  border-bottom: 1px solid #eee;
}
.param-item:last-child {
  border-bottom: none;
}
.param-label {
  color: #666;
  font-weight: bold;
}
.param-value {
  color: #2196f3;
}
.confirm-footer {
  padding: 15px 20px;
  border-top: 1px solid #eee;
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}
.btn-cancel {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 6px;
  cursor: pointer;
}
.btn-confirm {
  padding: 8px 16px;
  border: none;
  background: #4caf50;
  color: white;
  border-radius: 6px;
  cursor: pointer;
}
.btn-confirm:hover {
  background: #45a049;
}
</style>