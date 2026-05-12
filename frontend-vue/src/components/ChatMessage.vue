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
  display:flex;
}
.chat-msg--user {
  justify-content: flex-end;
}
.chat-msg--bot {
  justify-content: flex-start;
}
.chat-msg__bubble {
  max-width: 75%;
  padding: 10px 12px;
  border-radius: 8px;
  background: #fff;
  border: 1px solid #e6e6e6;
}
.chat-msg--user .chat-msg__bubble {
  background: #e6f7ff;
  border-color: #91d5ff;
}
.chat-msg__text {
  white-space: pre-wrap;
  word-break: break-word;
}
.chat-msg__time {
  font-size: 11px;
  color: #999;
  margin-top: 6px;
  text-align: right;
}
</style>
