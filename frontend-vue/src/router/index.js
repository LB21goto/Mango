import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Detail from '../views/Detail.vue'

const routes = [
  { path: '/',
    name: 'Home',
    component: Home },
  { path: '/detail/:id',
    name: 'Detail',
    component: Detail,
    props: true },
  {
    path: '/ai-chat',
    name: 'AIChat',
    component: () => import('../components/AIChat.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    // 优先恢复 history 保存的位置（后退/前进）；其它情况强制回到顶部
    if (savedPosition) {
      return savedPosition
    }
    // 支持 hash 跳转
    if (to.hash) {
      return { el: to.hash, top: 0 }
    }
    return { left: 0, top: 0 }
  }
})

export default router