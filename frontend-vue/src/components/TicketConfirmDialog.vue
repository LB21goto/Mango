<template>
  <Teleport to="body">
    <div v-if="visible" class="ticket-overlay" @click.self="handleCancel">
      <div class="ticket-dialog">
        <!-- 头部 -->
        <div class="ticket-header">
          <div class="header-icon">🎫</div>
          <h3>购票确认</h3>
          <button class="close-btn" @click="handleCancel">×</button>
        </div>

        <!-- 演出信息（可编辑） -->
        <div class="ticket-section">
          <div class="section-title">演出信息</div>
          <div class="form-grid">
            <div class="form-row">
              <label>演出名称</label>
              <input v-model="editableData.eventName" placeholder="请输入演出名称" />
            </div>
            <div class="form-row">
              <label>演出时间</label>
              <input v-model="editableData.showTime" placeholder="如：2024-06-15 20:00" />
            </div>
            <div class="form-row">
              <label>演出场馆</label>
              <input v-model="editableData.venue" placeholder="请输入场馆" />
            </div>
          </div>
        </div>

        <!-- 座位信息（可编辑） -->
        <div class="ticket-section">
          <div class="section-title">座位信息</div>
          <div class="form-grid">
            <div class="form-row">
              <label>区</label>
              <input v-model="editableData.area" placeholder="如：A" style="width: 60px" />
            </div>
            <div class="form-row">
              <label>排</label>
              <input v-model="editableData.row" placeholder="如：3" style="width: 60px" />
            </div>
            <div class="form-row">
              <label>座</label>
              <input v-model="editableData.seat" placeholder="如：15" style="width: 60px" />
            </div>
          </div>
          <div class="form-row" style="margin-top: 10px">
            <label>票价（元）</label>
            <input v-model.number="editableData.price" type="number" placeholder="1280" />
          </div>
          <div class="price-preview" v-if="editableData.price">
            合计：<span class="price">¥{{ editableData.price }}</span>
          </div>
        </div>

        <!-- 购票人信息（可编辑） -->
        <div class="ticket-section">
          <div class="section-title">购票人信息</div>
          <div class="form-grid">
            <div class="form-row">
              <label>姓名</label>
              <input v-model="editableData.buyerName" placeholder="请输入姓名" />
            </div>
            <div class="form-row">
              <label>手机号</label>
              <input v-model="editableData.buyerPhone" placeholder="请输入手机号" />
            </div>
            <div class="form-row">
              <label>身份证</label>
              <input v-model="editableData.buyerIdCard" placeholder="请输入身份证号" />
            </div>
          </div>
        </div>

        <!-- 风险提示 -->
        <div class="risk-notice">
          <span class="notice-icon">⚠️</span>
          <span>请确认以上信息无误，购票成功后将不可退换</span>
        </div>

        <!-- 底部操作 -->
        <div class="ticket-footer">
          <button class="btn-cancel" @click="handleCancel">取消</button>
          <button class="btn-confirm" @click="handleConfirm" :disabled="loading">
            <span v-if="loading" class="loading-spinner"></span>
            <span v-else>确认购票</span>
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  ticketInfo: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['confirm', 'cancel'])

const loading = ref(false)

// 可编辑的数据副本
const editableData = reactive({
  eventName: '',
  showTime: '',
  venue: '',
  area: '',
  row: '',
  seat: '',
  price: null,
  buyerName: '',
  buyerPhone: '',
  buyerIdCard: ''
})

// 当 ticketInfo 变化时，更新可编辑数据
watch(() => props.ticketInfo, (newVal) => {
  if (newVal) {
    editableData.eventName = newVal.eventName || ''
    editableData.showTime = newVal.showTime || ''
    editableData.venue = newVal.venue || ''
    editableData.area = newVal.seats?.[0]?.area || ''
    editableData.row = newVal.seats?.[0]?.row || ''
    editableData.seat = newVal.seats?.[0]?.seat || ''
    editableData.price = newVal.price || null
    editableData.buyerName = newVal.buyerInfo?.name || ''
    editableData.buyerPhone = newVal.buyerInfo?.phone || ''
    editableData.buyerIdCard = newVal.buyerInfo?.idCard || ''
  }
}, { immediate: true })

// 重置loading状态
watch(() => props.visible, (newVal) => {
  if (!newVal) {
    loading.value = false
  }
})

async function handleConfirm() {
  loading.value = true
  
  // 组装购票信息
  const result = {
    eventName: editableData.eventName,
    showTime: editableData.showTime,
    venue: editableData.venue,
    seats: [{
      area: editableData.area,
      row: editableData.row,
      seat: editableData.seat
    }],
    price: editableData.price,
    buyerInfo: {
      name: editableData.buyerName,
      phone: editableData.buyerPhone,
      idCard: editableData.buyerIdCard
    }
  }
  
  emit('confirm', result)
}

function handleCancel() {
  emit('cancel')
}
</script>

<style scoped>
.ticket-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.ticket-dialog {
  background: #fff;
  border-radius: 16px;
  width: 90%;
  max-width: 480px;
  max-height: 85vh;
  overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from { transform: translateY(20px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

.ticket-header {
  display: flex;
  align-items: center;
  padding: 20px 20px 16px;
  border-bottom: 1px solid #f0f0f0;
  position: relative;
}

.header-icon {
  font-size: 24px;
  margin-right: 10px;
}

.ticket-header h3 {
  flex: 1;
  margin: 0;
  font-size: 18px;
  color: #333;
}

.close-btn {
  position: absolute;
  right: 16px;
  top: 16px;
  width: 28px;
  height: 28px;
  border: none;
  background: #f5f5f5;
  border-radius: 50%;
  font-size: 18px;
  cursor: pointer;
  color: #666;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  background: #e6e6e6;
}

.ticket-section {
  padding: 16px 20px;
  border-bottom: 1px solid #f5f5f5;
}

.section-title {
  font-size: 13px;
  color: #999;
  margin-bottom: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.form-grid {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.form-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.form-row label {
  width: 70px;
  color: #666;
  font-size: 14px;
  flex-shrink: 0;
}

.form-row input {
  flex: 1;
  height: 36px;
  padding: 0 12px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}

.form-row input:focus {
  border-color: #ff4d4f;
  box-shadow: 0 0 0 2px rgba(255, 77, 79, 0.1);
}

.form-row input[type="number"] {
  width: 120px;
}

.price-preview {
  margin-top: 12px;
  padding: 8px 12px;
  background: #fff7e6;
  border-radius: 6px;
  color: #666;
  font-size: 14px;
}

.price {
  color: #ff4d4f;
  font-size: 18px;
  font-weight: 600;
}

.risk-notice {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: #fffbe6;
  color: #ad6800;
  font-size: 13px;
}

.notice-icon {
  font-size: 14px;
}

.ticket-footer {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
}

.btn-cancel,
.btn-confirm {
  flex: 1;
  height: 48px;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-cancel {
  background: #f5f5f5;
  border: 1px solid #d9d9d9;
  color: #666;
}

.btn-cancel:hover {
  background: #e6e6e6;
}

.btn-confirm {
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
  border: none;
  color: #fff;
}

.btn-confirm:hover:not(:disabled) {
  background: linear-gradient(135deg, #ff7875 0%, #ff4d4f 100%);
}

.btn-confirm:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.loading-spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
