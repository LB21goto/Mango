<template>
  <div :class="['chat-msg', role === 'user' ? 'chat-msg--user' : 'chat-msg--bot']">
    <div class="chat-msg__bubble">
      <div class="chat-msg__text" v-html="formattedText"></div>
      <div class="chat-msg__time">{{ time }}</div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
const props = defineProps({
  role: { type: String, required: true },
  text: { type: String, required: true },
  time: { type: String, default: '' }
});

const formattedText = computed(() => {
  // 简单处理换行为 <br>，可扩展支持富文本/Markdown
  return props.text ? props.text.replace(/\n/g, '<br/>') : '';
});
</script>

<style scoped>
.chat-msg {
  display: flex;
}

.chat-msg--user {
  justify-content: flex-end;
}

.chat-msg--bot {
  justify-content: flex-start;
}

.chat-msg__bubble {
  max-width: 72%;
  padding: 12px 16px;
  border-radius: 16px;
  background: #fff;
  border: 1px solid rgba(102, 126, 234, 0.1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.2s ease;
}

.chat-msg--user .chat-msg__bubble {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: transparent;
  color: white;
  border-bottom-right-radius: 4px;
}

.chat-msg--bot .chat-msg__bubble {
  border-bottom-left-radius: 4px;
}

.chat-msg__text {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.6;
  font-size: 14px;
}

.chat-msg--user .chat-msg__text {
  color: white;
}

.chat-msg__time {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 6px;
  text-align: right;
}

.chat-msg--bot .chat-msg__time {
  color: #a0aec0;
}
</style>
