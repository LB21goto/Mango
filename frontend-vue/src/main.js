import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './styles/styles.css'
import Home from './views/Home.vue'
import Detail from './views/Detail.vue'

const app = createApp(App)
app.use(router)
app.use(ElementPlus)
app.mount('#app')

const routes = [
    { path: '/', name: 'Home', component: Home },
    { path: '/detail/:id?', name: 'Detail', component: Detail, props: true }
]