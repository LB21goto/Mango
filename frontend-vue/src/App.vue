<template>
  <div class="app-root">
    <header class="app-header">
      <div class="header-inner">
        <img src="/images/title.png" class="app-logo" alt="logo" />

        <router-link to="/" class="nrv-link">
          <img src="/images/mainsite.png" alt="主站" class="site-icon" />
          首页
        </router-link>

        <el-input
          class="search" v-model="input"
          placeholder="活动 / 嘉宾 / 场馆" clearable
          @keyup.enter="onSearch" style="width: 320px;"
        >
          <!-- append 插槽通常只放图标按钮即可 -->
          <template #append>
            <!-- 添加 class="search-btn" -->
            <el-button type="primary" @click="onSearch" class="search-btn">
              <i class="el-icon-search"></i>
              <span>搜索</span>
            </el-button>
          </template>
        </el-input>
        <div class="button-row">
          <el-button round>个人订单</el-button>
        </div>
        <el-link href="http://localhost:3000/ai-chat" target="_blank" type="primary" :underline="false">
          🤖 AI助手
        </el-link>
      </div>
    </header>

    <main class="container">
      <!-- 使用路由占位符显示 Home / Detail -->
      <router-view />
    </main>
  </div>
</template>

<script>
import TicketForm from './components/TicketForm.vue';

export default {
  components: {
    TicketForm
  },
  name: 'App',
  data() {
    return {
      input: '',    // 绑定搜索关键词
      status: ''    // 保留你已有的状态字段
    };
  },
  methods: {
    updateStatus(newStatus) {
      this.status = newStatus;
    },
    onSearch() {
      // 搜索触发：当前只是示例，按需替换为路由跳转或接口调用
      if (!this.input) {
        this.$message && this.$message.warning('请输入搜索关键词');
        return;
      }
      // 示例：把输入打印到控制台并更新状态
      console.log('搜索：', this.input);
      this.status = `搜索：${this.input}`;
      // TODO: 在这里发起实际搜索请求或跳转到搜索结果页
      // 示例跳转到首页或实现搜索逻辑：
      // this.$router.push({ name: 'Home', query: { q: this.input } })
    }
    
  }
  
};
const buttons = [
  { type: '', text: 'plain' },
  { type: 'primary', text: 'primary' },
  { type: 'success', text: 'success' },
  { type: 'info', text: 'info' },
  { type: 'warning', text: 'warning' },
  { type: 'danger', text: 'danger' },
]
</script>

<style>
@import './styles/styles.css';

</style>