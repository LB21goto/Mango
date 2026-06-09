<template>
  <div class="my-orders">
    <div class="page-header">
      <h2>我的订单</h2>
    </div>

    <el-table :data="orders" stripe style="width: 100%" v-loading="loading">
      <el-table-column prop="orderNo" label="订单号" width="220" />
      <el-table-column prop="programId" label="节目ID" width="100" />
      <el-table-column prop="seatId" label="座位ID" width="100" />
      <el-table-column prop="price" label="价格" width="100">
        <template #default="{ row }">
          ¥{{ row.price }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'warning'">
            {{ row.status === 1 ? '已支付' : '未支付' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间">
        <template #default="{ row }">
          {{ formatTime(row.createTime) }}
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && orders.length === 0" description="暂无订单" />
  </div>
</template>

<script>
export default {
  name: 'MyOrder',
  data() {
    return {
      orders: [],
      loading: false,
      userId: 114514
    }
  },
  mounted() {
    this.fetchOrders()
  },
  methods: {
    async fetchOrders() {
      this.loading = true
      try {
        const res = await fetch(`http://localhost:6086/api/orders/my?userId=${this.userId}`)
        this.orders = await res.json()
      } catch (e) {
        this.$message?.error('加载订单失败')
      } finally {
        this.loading = false
      }
    },
    formatTime(time) {
      if (!time) return '-'
      return new Date(time).toLocaleString()
    }
  }
}
</script>

<style scoped>
.my-orders {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}
.page-header {
  margin-bottom: 20px;
}
.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}
</style>
