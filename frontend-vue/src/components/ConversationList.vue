<template>
  <aside class="conv-list" :class="{ open: visible }" @click.self="onBackdropClick">
    <div class="conv-list__panel">
      <div class="conv-list__header">
        <h3>会话</h3>
        <button class="close-btn" @click="$emit('close')">✕</button>
      </div>

      <div class="conv-list__actions">
        <button @click="$emit('new')" class="btn-new">新会话</button>
      </div>

      <ul class="conv-list__items">
        <li
            v-for="conv in conversations"
            :key="conv.id"
            :class="{ active: conv.id === activeId }"
            @click="$emit('select', conv.id)"
        >
          <div class="conv-title">{{ conv.title || '会话' }}</div>
          <div class="conv-sub">{{ conv.lastSummary || '暂无消息' }}</div>
          <button class="delete-btn" @click.stop="$emit('delete', conv.id)">删除</button>
        </li>
      </ul>

      <div class="conv-list__footer">
        <small>本地存储会话，仅保存在本机浏览器</small>
      </div>
    </div>
  </aside>
</template>

<script setup>
const props = defineProps({
  conversations: { type: Array, required: true },
  activeId: { required: false },
  visible: { type: Boolean, default: false }
});
const emit = defineEmits(['select', 'new', 'delete', 'close']);

function onBackdropClick() {
  // 点击遮罩关闭（移动端）
  emit('close');
}
</script>

<style scoped>
.conv-list {
  width: 0;
  transition: none;
}

.conv-list__panel {
  width: 280px;
  height: 100vh;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  border-right: 1px solid rgba(102, 126, 234, 0.1);
  box-shadow: 4px 0 20px rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
  position: relative;
}

.conv-list__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.08) 0%, rgba(118, 75, 162, 0.08) 100%);
  border-bottom: 1px solid rgba(102, 126, 234, 0.1);
}

.conv-list__header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.close-btn {
  background: transparent;
  border: none;
  font-size: 16px;
  cursor: pointer;
  color: #718096;
  padding: 4px 8px;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.close-btn:hover {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
}

.conv-list__actions {
  padding: 12px;
  border-bottom: 1px dashed rgba(102, 126, 234, 0.1);
}

.btn-new {
  width: 100%;
  padding: 10px 16px;
  border: none;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 10px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.25);
}

.btn-new:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.35);
}

.conv-list__items {
  list-style: none;
  margin: 0;
  padding: 8px;
  overflow: auto;
  flex: 1;
}

.conv-list__items li {
  padding: 12px;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  position: relative;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid transparent;
}

.conv-list__items li:hover {
  background: rgba(102, 126, 234, 0.05);
}

.conv-list__items li.active {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  border: 1px solid rgba(102, 126, 234, 0.2);
}

.conv-title {
  font-weight: 600;
  font-size: 14px;
  color: #2d3748;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.conv-sub {
  font-size: 12px;
  color: #718096;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.delete-btn {
  position: absolute;
  right: 8px;
  top: 8px;
  border: none;
  background: transparent;
  color: #e53e3e;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.2s ease;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.conv-list__items li:hover .delete-btn {
  opacity: 1;
}

.delete-btn:hover {
  background: rgba(229, 62, 62, 0.1);
}

/* Footer */
.conv-list__footer {
  padding: 12px;
  border-top: 1px solid rgba(102, 126, 234, 0.1);
  font-size: 11px;
  color: #a0aec0;
  text-align: center;
}

/* Desktop: 固定显示侧栏 */
@media (min-width: 900px) {
  .conv-list {
    width: 280px;
    flex: 0 0 280px;
    position: relative;
    transition: none;
  }

  .conv-list__panel {
    position: relative;
    box-shadow: none;
  }
}

/* Mobile: 侧栏抽屉覆盖，带遮罩效果 */
@media (max-width: 899px) {
  .conv-list {
    position: fixed;
    top: 0;
    left: 0;
    height: 100vh;
    width: 100%;
    display: flex;
    align-items: stretch;
    z-index: 1200;
    pointer-events: none;
  }

  .conv-list.open {
    pointer-events: auto;
  }

  /* 背景遮罩 */
  .conv-list::before {
    content: "";
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.3);
    backdrop-filter: blur(4px);
    opacity: 0;
    transition: opacity 0.25s;
    pointer-events: none;
  }

  .conv-list.open::before {
    opacity: 1;
    pointer-events: auto;
  }

  .conv-list__panel {
    transform: translateX(-100%);
    transition: transform 0.25s ease;
    pointer-events: auto;
  }

  .conv-list.open .conv-list__panel {
    transform: translateX(0);
  }
}
</style>
