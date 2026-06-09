<template>
  <Teleport to="body">
    <div v-if="visible" class="seat-overlay" @click.self="handleClose">
      <div class="seat-dialog">
        <!-- 头部 -->
        <div class="seat-header">
          <div class="header-icon"> </div>
          <h3>选择座位</h3>
          <button class="close-btn" @click="handleClose">×</button>
        </div>

        <!-- 座位图区域 -->
        <div class="seat-stage">舞台</div>
        <div class="seat-map-container">
          <div class="seat-map">
            <div v-for="(colCount, index) in seatsPerRow" :key="index + 1" class="seat-row">
              <span class="row-label">{{ index + 1 }}</span>
              <div class="seats">
                <button
                  v-for="col in colCount"
                  :key="col"
                  :class="[
                    'seat',
                    {
                      available: isAvailable(index + 1, col),
                      selected: isSelected(index + 1, col),
                      occupied: isOccupied(index + 1, col)
                    }
                  ]"
                  :disabled="isOccupied(index + 1, col)"
                  @click="toggleSeat(index + 1, col)"
                >
                  {{ col }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 图例 -->
        <div class="seat-legend">
          <div class="legend-item">
            <span class="seat available"></span>
            <span>可选</span>
          </div>
          <div class="legend-item">
            <span class="seat selected"></span>
            <span>已选</span>
          </div>
          <div class="legend-item">
            <span class="seat occupied"></span>
            <span>已售</span>
          </div>
        </div>

        <!-- 已选座位 -->
        <div class="selected-info" v-if="selectedSeats.length > 0">
          <div class="selected-label">已选座位：</div>
          <div class="selected-tags">
            <span
              v-for="seat in selectedSeats"
              :key="seat.key"
              class="seat-tag"
              @click="removeSeat(seat)"
            >
              {{ seat.row }}排{{ seat.col }}座 ×
            </span>
          </div>
          <div class="total-price">
            合计：<span class="price">¥{{ totalPrice }}</span>
          </div>
        </div>

        <!-- 底部操作 -->
        <div class="seat-footer">
          <el-button @click="handleClose">取消</el-button>
          <el-button type="primary" :disabled="selectedSeats.length === 0" @click="handleConfirm">
            确认选座 ({{ selectedSeats.length }}张)
          </el-button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  price: {
    type: Number,
    default: 88
  },
  seatsPerRow: {
    type: Array,
    default: () => [4, 6, 10]
  }
})

const emit = defineEmits(['update:visible', 'confirm'])

const selectedSeats = ref([])

// 最大座位数，用于统一每行宽度实现居中
const maxSeats = computed(() => Math.max(...props.seatsPerRow))

// 随机生成已售座位（模拟数据）
const occupiedSeats = ref(new Set())

// 从后端获取已售座位
const fetchSoldSeats = async () => {
  try {
    const eventId = props.eventId || 1
    const res = await fetch(`http://localhost:6086/ticket/seats/sold?eventId=${eventId}`)
    const data = await res.json()
    if (Array.isArray(data)) {
      occupiedSeats.value = new Set(data)
    }
  } catch (e) {
    console.error('获取已售座位失败:', e)
    // 失败时使用随机模拟数据
    initOccupiedSeats()
  }
}

// 计算全局座位ID（从1开始递增）
const getGlobalSeatId = (row, col) => {
  let globalId = 0
  for (let r = 1; r <= row; r++) {
    const colCount = props.seatsPerRow[r - 1] || 0
    if (r === row) {
      globalId += col
    } else {
      globalId += colCount
    }
  }
  return globalId
}

// 初始化时随机生成一些已售座位
const initOccupiedSeats = () => {
  const occupied = new Set()
  let globalSeatId = 0
  props.seatsPerRow.forEach((colCount) => {
    for (let col = 1; col <= colCount; col++) {
      globalSeatId++
      if (Math.random() < 0.3) {
        occupied.add(globalSeatId)
      }
    }
  })
  occupiedSeats.value = occupied
}

const isAvailable = (row, col) => {
  return !occupiedSeats.value.has(getGlobalSeatId(row, col))
}

const isOccupied = (row, col) => {
  return occupiedSeats.value.has(getGlobalSeatId(row, col))
}

const isSelected = (row, col) => {
  return selectedSeats.value.some(s => s.row === row && s.col === col)
}

const toggleSeat = (row, col) => {
  if (isOccupied(row, col)) return

  const globalSeatId = getGlobalSeatId(row, col)
  const index = selectedSeats.value.findIndex(s => s.row === row && s.col === col)
  if (index > -1) {
    selectedSeats.value.splice(index, 1)
  } else {
    selectedSeats.value.push({ row, col, key: `${row}-${col}`, seatId: globalSeatId })
  }
}

const removeSeat = (seat) => {
  const index = selectedSeats.value.findIndex(s => s.key === seat.key)
  if (index > -1) {
    selectedSeats.value.splice(index, 1)
  }
}

const totalPrice = computed(() => {
  return selectedSeats.value.length * props.price
})

const handleClose = () => {
  emit('update:visible', false)
}

const handleConfirm = () => {
  console.log('SeatSelector emit confirm, selectedSeats:', JSON.stringify(selectedSeats.value));
  emit('confirm', {
    seats: selectedSeats.value.map(s => ({ seatId: s.seatId, row: s.row, col: s.col })),
    totalPrice: totalPrice.value
  })
  handleClose()
}

// 当弹窗显示时初始化
const handleVisibleChange = (val) => {
  if (val) {
    selectedSeats.value = []
    fetchSoldSeats()
  }
}

defineExpose({
  handleVisibleChange,
  fetchSoldSeats
})
</script>

<style scoped>
.seat-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.seat-dialog {
  background: #fff;
  border-radius: 12px;
  width: 90%;
  max-width: 800px;
  max-height: 90vh;
  overflow-y: auto;
  padding: 20px;
}

.seat-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  position: relative;
}

.seat-header h3 {
  margin: 0;
  font-size: 18px;
}

.header-icon {
  font-size: 24px;
}

.close-btn {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  font-size: 28px;
  cursor: pointer;
  color: #999;
  line-height: 1;
}

.close-btn:hover {
  color: #333;
}

.seat-stage {
  background: linear-gradient(135deg, #ff6b9b, #ff8fab);
  color: #fff;
  text-align: center;
  padding: 8px 20px;
  border-radius: 6px;
  margin-bottom: 20px;
  font-size: 14px;
  letter-spacing: 4px;
}

.seat-map-container {
  overflow-x: auto;
  padding: 10px 0;
  display: flex;
  justify-content: center;
}

.seat-map {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.seat-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  position: relative;
}

.row-label {
  position: absolute;
  left: -30px;
  width: 24px;
  text-align: center;
  font-size: 12px;
  color: #666;
}

.seats {
  display: flex;
  gap: 0;
  justify-content: center;
}

.seat {
  width: 28px;
  height: 28px;
  border-radius: 4px;
  border: 1px solid #ddd;
  background: #fff;
  cursor: pointer;
  font-size: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.seat.available {
  background: #e8f5e9;
  border-color: #81c784;
}

.seat.available:hover {
  background: #c8e6c9;
  transform: scale(1.1);
}

.seat.selected {
  background: #ff6b9b;
  border-color: #ff6b9b;
  color: #fff;
}

.seat.occupied {
  background: #eee;
  border-color: #ddd;
  color: #bbb;
  cursor: not-allowed;
}

.seat-legend {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin: 16px 0;
  font-size: 13px;
  color: #666;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.legend-item .seat {
  width: 20px;
  height: 20px;
  cursor: default;
}

.selected-info {
  background: #fafafa;
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 16px;
}

.selected-label {
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
}

.selected-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}

.seat-tag {
  background: #fff0f6;
  color: #ff6b9b;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
}

.seat-tag:hover {
  background: #ffe6ee;
}

.total-price {
  text-align: right;
  font-size: 15px;
  color: #333;
}

.total-price .price {
  color: #ff6b9b;
  font-weight: 600;
  font-size: 18px;
}

.seat-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #eee;
}
</style>
