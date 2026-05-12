# Vue.js 演出抢票项目

这是一个使用 Vue.js 构建的演出抢票前端示例项目。该项目展示了如何与后端 API 进行交互，用户可以输入用户 ID 和座位 ID 来抢票或释放座位。

## 项目结构

```
frontend-vue
├── index.html          # 应用的主 HTML 文件
├── package.json        # npm 配置文件，列出项目依赖项和脚本
├── vite.config.js      # Vite 配置文件
├── .gitignore          # Git 忽略文件
├── src
│   ├── main.js         # 应用入口文件
│   ├── App.vue         # 主组件文件
│   ├── api.js          # 与后端 API 的交互
│   ├── components
│   │   └── TicketForm.vue # 用户输入表单组件
│   └── styles
│       └── styles.css  # 样式文件
└── README.md           # 项目文档和使用说明
```

## 安装与运行

1. 克隆项目到本地：
   ```
   git clone <repository-url>
   ```

2. 进入项目目录：
   ```
   cd frontend-vue
   ```

3. 安装依赖：
   ```
   npm install
   ```

4. 启动开发服务器：
   ```
   npm run dev
   ```

5. 打开浏览器访问 `http://localhost:3000`（或终端中显示的其他地址）。

## 使用说明

- 在用户 ID 输入框中输入您的用户 ID。
- 在座位 ID 输入框中输入您想要抢购的座位 ID。
- 点击“立即抢票”按钮进行抢票操作。
- 点击“释放座位”按钮释放已占用的座位。

## 注意事项

- 请确保后端服务已启动，并且接口地址正确。
- 如果遇到跨域（CORS）问题，请参考项目中的联调建议。

## 贡献

欢迎任何形式的贡献！请提交问题或拉取请求。