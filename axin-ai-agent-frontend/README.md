# AI智能助手前端项目

这是一个基于Vue3的AI智能助手前端项目，包含两个AI聊天应用：AI计划大师和智多星AI智能体。

## 功能特性

- 🏠 **主页**：应用选择界面，支持切换不同的AI应用
- 📋 **AI计划大师**：专业的计划制定助手，通过SSE实时对话
- 🤖 **智多星AI智能体**：全能AI助手，解答各种问题
- 💬 **实时聊天**：基于SSE的实时消息传输
- 📱 **响应式设计**：支持桌面端和移动端

## 技术栈

- Vue 3 (Composition API)
- Vue Router 4
- Axios
- Vite
- CSS3 (渐变、动画、响应式)

## 项目结构

```
src/
├── api/           # API配置
├── router/        # 路由配置
├── utils/         # 工具函数 (SSE客户端)
├── views/         # 页面组件
│   ├── Home.vue      # 主页
│   ├── PlanApp.vue   # AI计划大师
│   └── ManusApp.vue  # 智多星AI智能体
├── App.vue        # 根组件
├── main.js        # 入口文件
└── style.css      # 全局样式
```

## 安装和运行

1. 安装依赖：
```bash
npm install
```

2. 启动开发服务器：
```bash
npm run dev
```

3. 访问应用：
打开浏览器访问 `http://localhost:5173`

## 后端接口

项目需要配合SpringBoot后端运行，后端接口地址：`http://localhost:8123/api`

### 接口说明

1. **AI计划大师接口**：
   - 路径：`/ai/plan_app/chat/sse_emitter`
   - 方法：GET
   - 参数：`message`（消息内容）、`chatId`（聊天室ID）
   - 返回：SSE流

2. **智多星AI智能体接口**：
   - 路径：`/ai/manus/chat`
   - 方法：GET
   - 参数：`message`（消息内容）
   - 返回：SSE流

## 功能说明

### 主页
- 展示两个AI应用的介绍
- 提供"开始聊天"按钮进入对应应用
- 响应式设计，支持移动端

### AI计划大师
- 自动生成唯一的聊天室ID
- 实时SSE连接，支持流式响应
- 聊天记录显示（用户消息在右侧，AI回复在左侧）
- 输入框支持多行文本和回车发送

### 智多星AI智能体
- 功能与AI计划大师类似
- 调用不同的后端接口
- 相同的UI设计和交互体验

## 开发说明

- 使用Vue 3 Composition API
- SSE连接通过自定义工具类管理
- 响应式设计，支持各种屏幕尺寸
- 现代化的UI设计，包含渐变背景和动画效果

## 注意事项

1. 确保后端服务已启动并运行在 `http://localhost:8123`
2. 前端运行在 `http://localhost:5173`
3. 需要处理跨域问题（如果后端未配置CORS）
4. SSE连接在组件卸载时会自动关闭
