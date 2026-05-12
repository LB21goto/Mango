<template>
  <div class="ticket-root">
    <!-- 顶部：左海报 + 右购票面板 -->
    <div class="product-row">
      <div class="poster">
        <!-- 优先显示后端 cover_image（去掉多余空格），没有则显示占位图 -->
        <img v-if="event && event.cover_image" :src="event.cover_image.trim()" alt="海报" />
        <img v-else src="/images/mainpost.jpeg" alt="海报占位" />
      </div>

      <div class="purchase-panel">
        <h1 class="title">{{ event?.title || '加载中...' }}</h1>
        <div class="meta">
          <!-- <div>时间：{{ eventDateFormatted || '—' }}</div> -->
          <div class="event-time">时间：2026.04.11（以现场为准）</div>
          <div class="changguan">场馆：{{ event?.venue || '—' }}</div>
        </div>

        <div class="selectors">
          <p2>场次: </p2>
          <el-button
        v-for="s in sessions"
        :key="s.id"
        plain
        :class="['session-btn', { active: selectedSessionId === s.id }]"
        @click="selectSession(s.id)"
      >
        <div class="session-line">
          <div class="session-time">{{ s.time }}</div>
          <div class="session-venue">{{ s.venue || s.note }}</div>
        </div>
      </el-button>
          

          <!-- 改为可选（互斥）按钮 -->
          <div class="price-options">
            <p2>票档：</p2>
            <el-button
              plain
              :class="['price-btn', { active: selectedPrice === 'normal' }]"
              @click="selectPrice('normal')"
            >¥88 (普通券)</el-button>

            <el-button
              plain
              :class="['price-btn', { active: selectedPrice === 'vip' }]"
              @click="selectPrice('vip')"
            >¥128 (VIP 券)</el-button>
          </div>
          <div class="qty">
            <span>数量</span>
            <el-input-number v-model="qty" :min="1" :controls="true" />
          </div>
        </div>

        <div class="actions">
          <el-button type="primary" class="buy-btn" :loading="loading" @click="createTicket">立即购票</el-button>
          <div class="likes">❤ 597人想去</div>
        </div>
        <div class="notice">电子票/凭证票，无需邮寄。不支持7天无理由退票</div>
      </div>
    </div>

    <!-- 中部：选项卡 + 内容区域（主内容 + 侧栏信息卡） -->
    <div class="tabs-row">
      <div class="tabs">
        <button class="tab active">活动详情</button>
        <button class="tab normal">讨论</button>
      </div>

      <div class="content-grid">
        <div class="main">
          <el-card class="detail-card">
            <h3>活动介绍</h3>
            <!-- <img src="/images/program1/detail{{event?.id}}.jpg" alt="介绍图" class="big-gray-img" /> -->
             <img v-if="event" :src="detailImage" alt="介绍图" class="big-gray-img" />
            <img v-else src="/images/program1/detail2.jpg" alt="介绍图占位" class="big-gray-img" />
          </el-card>
        </div>

        <!-- 合并后的侧栏：内部竖直排列两个卡片 -->
        <aside class="aside">
          <el-card class="info-card organizer">
            <h4>主办方信息</h4>
            <p>主办：某某文化</p>
            <p>苏州麋亿次元文化传媒有限公司</p>
          </el-card>

          <el-card class="info-card buy-notice">
            <h4>购票须知</h4>
            <ul>
              <p>1、会员购仅作为演出/赛事项目的票务代理方，不承担除法律明确规定外的责任。若您与主办方之间出现争议，会员购将尽力为您协调处理保障您的利益，但无法保证协调结果符合您的期望，会员购无权对争议作出处理。</p>

              <p>2、会员购在线售票为电子票或纸质票。如您购买的是电子票，入场时请在bilibili APP中的【订单中心】打开电子票检票入场（部分活动需兑换实体票入场）；电子票不得转售，请在正规途径购买；电子票截图无法入场。如您购买的是纸质票，需在购票时填写准确配送信息，纸质票将在活动开始之前寄出，邮费由用户承担；因纸质票的特殊性，遗失不补且无法挂失，请您保管好购买到的票品。
              </p>
              <p>
                、因票品具有稀缺性和时效性的特点，一旦退货将会影响二次销售，票品均不支持七天无理由退票，请您理解并审慎购买，并非所有项目主办方都支持因个人原因发起的退票，具体请以各商品展示页说明信息为准，请您在购买前仔细阅读商品信息。
              </p>

            </ul>
          </el-card>
        </aside>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'TicketForm',
  props: { eventId: { type: [String, Number], default: null } }, // 若已有可合并
  data() {
    return {
      date: null,
      qty: 1,
      loading: false,
      // 新增：记录当前选中的价格类型
      selectedPrice: 'normal', // 默认选中普通券
      priceMap: { normal: 88, vip: 128 }, // 可按需调整
      // 静态场次测试数据（测试用），打开页面即可选择
    sessions: [
    ],
      selectedSessionId: 2,
      event: null, // 新增：保存后端返回的节目信息
      
    };
  },
  mounted() {
    console.log('TicketForm mounted, eventId=', this.eventId);
    if (this.eventId) this.fetchProgram();
  },
  computed: {
    detailImage() {
      // 保证路径以 / 开头，文件放在 public/images/program1/detail<ID>.jpg
      const id = this.event?.id ;
      return `/images/program1/detail${id}.jpg`;
    },
    eventDateFormatted() {
      const t = this.event && (this.event.first_session || this.event.date || this.event.session_time);
      if (!t) return '';
      const d = new Date(t);
      return isNaN(d) ? t : d.toLocaleString();
    },
    // 方便取当前选中的价格数值
    selectedPriceValue() {
      return this.priceMap[this.selectedPrice] ?? null;
    }
  },
  methods: {
    selectPrice(type) {
      this.selectedPrice = type;
      // 若有表单字段需要同步，可在这里赋值，例如：
      // this.form = this.form || {};
      // this.form.price = this.selectedPriceValue;
    },
    async requestJsonOrText(url) {
      const res = await fetch(url);
      const text = await res.text();
      try { return JSON.parse(text); } catch { return { message: text || (res.ok ? '操作成功' : '服务返回错误') }; }
    },
    async createTicket() {
      if (!this.selectedSessionId) {
        this.$message?.warning('请选择场次');
        return;
      }
      this.loading = true;
      try {
        const payload = {
          userId: 114514, // 若有登录请填入真实 userId
          seatId: this.selectedSessionId, // or 传具体座位 id，按后端定义
          eventId: this.eventId // 组件 prop，确保有值
        };

        // 把 host:port 改为后端实际监听端口（例如 8080 或 6086）
        const res = await fetch('http://localhost:6086/ticket/seat/create', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });

        // 你的后端返回 String -> 用 text() 解析；若后端改为 JSON 请改成 res.json()
        const text = await res.text();

        if (res.ok) {
          this.$message?.success(text || '下单请求已提交');
          this.status = text || '下单成功';
        } else {
          this.$message?.error(text || '下单失败');
          this.status = text || '下单失败';
        }
      } catch (e) {
        console.error(e);
        this.$message?.error('网络或服务异常');
        this.status = '网络或服务异常';
      } finally {
        this.loading = false;
      }
    },
    // ...existing methods...
    async fetchProgram() {
      if (!this.eventId) {
        console.warn('fetchProgram: missing eventId');
        return;
      }
      console.log('fetchProgram ->', this.eventId);
      try {
        const res = await fetch(`http://localhost:6086/api/program/${this.eventId}`);
        const text = await res.text();
        console.log('fetchProgram response status:', res.status, 'body:', text);
        if (!res.ok) {
          this.$message?.error(`加载活动失败: ${res.status}`);
          return;
        }

        const json = JSON.parse(text);
        // 把活动对象写入 this.event，兼容不同 DTO 结构
        console.log('program json parsed:', json); // 调试输出

        // 规范化封面字段（兼容 coverImage / cover_image / cover）
        const srcRaw = json.coverImage || json.cover_image || json.cover || (json.event && (json.event.coverImage || json.event.cover_image || json.event.cover));
        const cover = typeof srcRaw === 'string' ? srcRaw.trim().replace(/^['"]|['"]$/g, '') : srcRaw;

        // 取到活动对象并确保有 cover_image 字段
        const evt = json.event || json;
        evt.cover_image = cover || evt.cover_image || '';

        
        

        this.event = json.event || json;
        console.log('resolved event:', this.event); // 调试输出
        const s = json.sessions || json.sessionList || [];
        this.sessions = s.map(item => ({ id: item.id, time: item.session_time || item.time, venue: item.venue, note: item.note }));

        if (this.sessions.length) this.selectedSessionId = this.sessions[0].id;
        if (json.tickets && json.tickets.length) {
          this.priceMap.normal = json.tickets[0].price ?? this.priceMap.normal;
          if (json.tickets[1]) this.priceMap.vip = json.tickets[1].price ?? this.priceMap.vip;
        }
      } catch (e) {
        console.error('fetchProgram exception:', e);
        this.$message?.error('请求异常，查看控制台或后端日志');
      }
    },

    
  }

    
  
};
</script>

<style scoped>
/* 顶部布局 */
.product-row {
  display: flex;
  gap: 32px;
  align-items: flex-start;
  margin: 20px 0;
}
.poster {
  flex: 0 0 300px; /* 海报宽度，可调 */
}
.poster img {
  width: 100%;
  height: auto;
  border-radius: 6px;
  box-shadow: 0 6px 18px rgba(0,0,0,0.08);
  display: block;
}
.purchase-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.title {
  margin: 0 0 8px;
  font-size: 22px;
  font-weight: 600;
}
.meta { color: #666; font-size: 13px; margin-bottom: 14px; }

/* 选择区 */
.selectors { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; margin-bottom: 14px; }
.price-options { display:flex; gap:8px; align-items:center; flex-basis: 100% }
.price-btn { border-color: #ffdce6; color: #ff6b9b; }
.price-btn.active { background:#fff0f6; color:#ff6b9b; }

/* 操作区 */
.actions { display:flex; align-items:center; gap:16px; margin:12px 0; }
.buy-btn {
  background: #ff6b9b;
  border-color: #ff6b9b;
  color: #fff;
  padding: 12px 32px;
}
.buy-btn:hover { background:#ff5a85; border-color:#ff5a85; }
.likes { color:#999; font-size:13px; }

/* 提示文案 */
.notice { color:#999; font-size:12px; margin-top:8px; }

/* 选项卡与内容区 */
.tabs { display:flex; gap:8px; margin: 24px 0; }
.tab { background:#fff; border:1px solid #eef0f3; padding:8px 16px; border-radius:6px; cursor:pointer; }
.tab.active { border-bottom: 2px solid #ff6b9b; color:#ff6b9b; }
.tab.normal {  color:#0b0a0a; }

/* 主内容 + 侧栏 */
.content-grid { display:flex; gap:20px; }
.main { flex: 1; }
.aside { width: 320px; }



/* 卡片样式 */
.detail-card, .info-card {
  padding: 18px;
  border-radius: 8px;
  box-shadow: 0 6px 18px rgba(0,0,0,0.04);
  border: none;
}

/* 响应式 */
@media (max-width: 900px) {
  .product-row { flex-direction: column; }
  .poster { flex-basis: 100%; max-width: 420px; margin: 0 auto; }
  .aside { width: 100%; }
  .content-grid { flex-direction: column; }
}
.selectors { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; margin-bottom: 14px; }
.selectors .qty {
  flex: 0 0 100%;    /* 占满一行，换行展示 */
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}
/* 覆盖全局样式，让当前标题靠左 */
.purchase-panel .title {
  text-align: left;
  align-self: flex-start;
}
/* scoped 文件中需用深度选择器匹配 el-input-number 的内部结构 */
::v-deep .qty .el-input-number,
::v-deep .qty .el-input-number__controls {
  border: none;          /* 去掉外层容器边框 */
  box-shadow: none;
  background: transparent;
}
/* 只让内部输入框显示一条边框（单框效果） */
::v-deep .qty .el-input__inner {
  border: 0px solid #eee;
  border-radius: 6px;
  padding: 6px 10px;
  height: 34px;
  box-sizing: border-box;
}
/* 侧栏改为竖向排列，卡片间距 */
.aside {
  width: 320px;
  display: flex;
  flex-direction: column;
  gap: 16px;
} 
.info-card.buy-notice {
  flex: 1;
  display: flex;
  /* 关键：让 flex 的子元素横向占满 100% */
  align-items: stretch; 
}/* 如果文字外面还包了一层 <p> 或 <div>，给那层加宽度 */
.info-card.buy-notice p {
  width: 100%; 
}
/* 侧栏卡片内文字更紧凑 */
.info-card p,
.info-card ul,
.info-card li {
  margin: 0;
  padding: 0;
  color: #666;
  font-size: 13px;
  line-height: 1.6;
}
/* price-btn.active 已有样式；若需加强，可加如下： */
.price-btn.active {
  background: #fff0f6;
  border-color: #ff6b9b;
  color: #ff6b9b;
}
.session-btn {
  border: 1px solid #ffdce6;
  color: #ff6b9b;
  background: transparent;
  padding: 8px 12px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: all 0.15s ease;
}
/* 选中态（与 price-btn.active 一致） */
.session-btn.active {
  background: #fff0f6;
  border-color: #ff6b9b;
  color: #ff6b9b;
  box-shadow: 0 6px 16px rgba(255,107,155,0.08);
}
.aside .el-card__body {
  padding: 10px 15px; 
}
.aside ul {
  padding-left: 15px; /* 你觉得合适就行，20px 也可以 */
  margin: 0;          /* 清除默认外边距 */
}

.aside ul li {
  margin-bottom: 5px; /* 让每一行购票须知稍微拉开点距离 */
}
/* 1. 干掉 h4 标签默认的巨大上边距和下边距 */
.aside h4 {
  margin: 0 0 10px 0; /* 上0，右0，下10px(稍微和正文隔开点)，左0 */
}
.event-time {
  font-size: 16px; /* 或者更大，比如 20px */
  font-weight: 400; /* 可选：加粗让字体看起来更显眼 */
  color: #303133;   /* 可选：稍微调深一点颜色 */
}
.changguan {
  gap: 33px; /* 原来是 8px，现在改成 16px 或者 20px，间距立马变大 */
  display: flex; /* 确保你是用了 flex 布局，gap 才会生效 */
  flex-direction: column; /* 确保是纵向排列 */
}

</style>