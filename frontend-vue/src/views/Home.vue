<template>
  <div class="home-page">
    <!-- 大横幅（使用现有 header 的 logo） -->
    
        <div class="hero-carousel">
        <el-carousel height="450px" indicator-position="outside" :autoplay="true" :interval="4000">
            <el-carousel-item v-for="(img, idx) in heroImages" :key="idx">
            <img :src="img" class="hero-slide" alt="hero" />
            </el-carousel-item>
        </el-carousel>
    </div>

    
    <!-- 分类图标行 -->
    <div class="category-row">
      <div class="cat" v-for="c in categories" :key="c.text">
        <img :src="c.icon" alt="" class="cat-icon" />
        <div class="cat-text">{{ c.text }}</div>
      </div>
    </div>

    <!-- 推荐板块（卡片列表） -->
    <section class="section">
      <div class="section-head">
        <h2>漫展</h2>
        <router-link to="/category/1" class="more">查看全部</router-link>
      </div>

      <div class="cards">
        <router-link v-for="(p, i) in programs" :key="i" :to="{ name: 'Detail', params: { id: p.id }}" class="card">
          <img :src="p.img" alt="" class="card-img" />
          <div class="card-info">
            <div class="card-title">{{ p.title }}</div>
            <div class="card-meta">
              <span class="date">{{ p.date }}</span>
              <span class="price">{{ p.price }} </span>
            </div>
          </div>
        </router-link>
      </div>
    </section>
    <section class="section">
        <div class="section-head">
            <h2>演唱会</h2>
            <router-link to="/category/1" class="more">查看全部</router-link>
        </div>

        <div class="cards">
            <router-link v-for="(p, i) in programs1" :key="i" :to="{ name: 'Detail', params: { id: p.id }}" class="card">
            <img :src="p.img" alt="" class="card-img" />
            <div class="card-info">
                <div class="card-title">{{ p.title }}</div>
                <div class="card-meta">
                <span class="date">{{ p.date }}</span>
                <span class="price">{{ p.price }} 起</span>
                </div>
            </div>
            </router-link>
        </div>
    </section>
    
  </div>
</template>

<script>
export default {
  name: 'HomeView',
  data() {
      return {
        heroImages: [
        '/images/zmd1.jpeg',
        '/images/zmd2.jpg',
        '/images/zmd3.jpg'
      ],
        categories: [
            { text: '演唱会', icon: '/images/music.jpg' },
            { text: '话剧歌剧', icon: '/images/drama.jpg' },
            { text: '体育', icon: '/images/sports.jpg' },
            { text: '二次元', icon: '/images/anime.jpg' },
            { text: '展览', icon: '/images/expo.jpg' }
        ],
        programs: [
            { id: 2, title: '上海·孤独摇滚 同人ONLY', img: '/images/mainpost.jpeg', date: '2026-04-11', price: '¥150' },
            { id: 3, title: '北京花朝汉服文化节', img: '/images/program1/hanfu.jpeg', date: '2026-04-20', price: '¥188' },
            { id: 4, title: '上海星享时光漫展', img: '/images/program1/fuxing.jpeg', date: '2026-05-05', price: '¥269' }
          ],
        programs1: [
            { id: 1, title: '【挪威的森林】伍佰魔性情歌之夜演唱会|从“突然的自我”听到“伤心太平洋”', img: '/images/program2/music01.jpg', date: '2026-04-11', price: '¥130' },
            { id: 2, title: '千禧隧道的「十年」”——《断桥残雪》《十年》《青花瓷》经典名曲live现场', img: '/images/program2/music02.webp', date: '2026-04-20', price: '¥288' },
            { id: 3, title: '林志炫《我忘了我已老去》巡回演唱会', img: '/images/program2/music02.jpg', date: '2026-05-05', price: '¥289' }
        ]
      }
    },
}
</script>

<style scoped>
.home-page { max-width: 980px; margin: 0 auto; padding: 18px; box-sizing: border-box; }

/* 横幅 */
.hero { display:flex; justify-content:center; align-items:center; padding: 18px 0 8px; }
.hero-logo { max-width: 420px; width: 60%; height: auto; display:block; }

/* 分类 */
.category-row { display:flex; gap:86px; justify-content:flex-start; align-items:center; background:#fff; padding:14px; margin:18px 0; border-radius:8px; box-shadow: 0 2px 8px rgba(0,0,0,0.03); }
.cat { width:100px; text-align:center; font-size:13px; color:#666; display:flex; flex-direction:column; align-items:center; gap:8px; }
.cat-icon { width:42px; height:42px; object-fit:contain; }

/* 板块头 */
.section { margin-top: 12px; }
.section-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.section-head h3 { margin:0; font-size:18px; }
.more { color:#999; font-size:13px; text-decoration:none; }

/* 卡片行 */
.cards { display:flex; gap:36px; }
.card { display:block; width:300px; text-decoration:none; color:inherit; background:#fff; border-radius:8px; overflow:hidden; box-shadow:0 6px 18px rgba(0,0,0,0.04); }
.card-img { width:100%; height: auto; object-fit:cover; display:block; }
.card-info { padding:10px 12px; }
.card-title { font-size:14px; font-weight:600; margin-bottom:8px; color:#333; }
.card-meta { display:flex; justify-content:space-between; color:#ff6b9b; font-weight:600; }

/* 响应式 */
@media (max-width: 900px) {
  .hero-logo { width: 80%; max-width: 320px; }
  .cards { flex-wrap:wrap; }
  .card { width:48%; }
  .category-row { overflow:auto; }
}

.hero-carousel { 
    width: 100vw;
  margin-left: calc(50% - 50vw); /* 拉伸到视口两边 */
  max-width: none;
  padding: 12px 0;
  box-sizing: border-box;
}
.hero-slide { width: 100%; 
    height: 450px; 
    object-fit:cover; 
    display: block; 
    border-radius: 8px; 
    background-color: #f5f5f5; /* 给留白处加个背景色，好看点 */
}
/* hero 内部内容仍居中 */
.hero-carousel .el-carousel {
    max-width: 1000px;
    margin: 0 auto;
}

</style>