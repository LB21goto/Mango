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
  /* 覆盖在左侧的抽屉样式（移动端） */
}
.conv-list__panel {
  width: 280px;
  height: 100vh;
  background: #fff;
  border-right: 1px solid #e6e6e6;
  box-shadow: 2px 0 12px rgba(0,0,0,0.06);
  display:flex;
  flex-direction:column;
  position: relative;
}
.conv-list__header {
  display:flex;
  align-items:center;
  justify-content:space-between;
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
}
.close-btn {
  background:transparent;
  border:none;
  font-size:16px;
  cursor:pointer;
}
.conv-list__actions {
  padding: 8px 12px;
  border-bottom: 1px dashed #f0f0f0;
}
.conv-list__items {
  list-style:none;
  margin:0;
  padding:8px;
  overflow:auto;
  flex:1;
}
.conv-list__items li {
  padding:10px;
  border-radius:6px;
  display:flex;
  flex-direction:column;
  gap:6px;
  position:relative;
  cursor:pointer;
}
.conv-list__items li.active {
  background:#f0f7ff;
  border:1px solid #d6eaff;
}
.conv-title {
  font-weight:600;
  white-space:nowrap;
  overflow:hidden;
  text-overflow:ellipsis;
}
.conv-sub {
  font-size:12px;
  color:#888;
  white-space:nowrap;
  overflow:hidden;
  text-overflow:ellipsis;
}
.delete-btn {
  position:absolute;
  right:8px;
  top:8px;
  border:none;
  background:transparent;
  color:#ff4d4f;
  cursor:pointer;
}

/* Footer */
.conv-list__footer {
  padding:12px;
  border-top:1px solid #f0f0f0;
  font-size:12px;
  color:#888;
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
    position:absolute;
    inset:0;
    background: rgba(0,0,0,0.3);
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
    transition: transform 0.25s;
    pointer-events: auto;
  }
  .conv-list.open .conv-list__panel {
    transform: translateX(0);
  }
}
</style>
