<template>
  <div class="chat-container">
    <div class="chat-header">
      <button class="btn btn-secondary back-btn" @click="goBack">
        ← 返回主页
      </button>
      <h1 class="chat-title">AI计划大师</h1>
      <div class="chat-id">会话ID: {{ chatId }}</div>
    </div>
    
    <div class="chat-messages" ref="messagesContainer">
      <div 
        v-for="message in messages" 
        :key="message.id"
        :class="['message', message.type]"
      >
        <div class="message-content">
          <div class="message-text" v-html="formatMessage(message.content)"></div>
          <div class="message-time">{{ formatTime(message.timestamp) }}</div>
        </div>
      </div>
      
      <div v-if="isLoading" class="message ai">
        <div class="message-content">
          <div class="typing-indicator">
            <span></span>
            <span></span>
            <span></span>
          </div>
        </div>
      </div>
    </div>
    
    <div class="chat-input-container">
      <div class="input-wrapper">
        <textarea
          v-model="inputMessage"
          @keydown.enter.prevent="sendMessage"
          placeholder="请输入您的计划需求..."
          class="chat-input"
          :disabled="isLoading"
          rows="3"
        ></textarea>
        <button 
          @click="sendMessage" 
          class="send-btn"
          :disabled="!inputMessage.trim() || isLoading"
        >
          发送
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { SSEClient, generateChatId } from '../utils/sse.js'
import { marked } from 'marked'

export default {
  name: 'PlanApp',
  setup() {
    const router = useRouter()
    const messages = ref([])
    const inputMessage = ref('')
    const isLoading = ref(false)
    const chatId = ref('')
    const messagesContainer = ref(null)
    let sseClient = null

    // 生成聊天室ID
    const initChatId = () => {
      chatId.value = generateChatId()
    }

    // 返回主页
    const goBack = () => {
      if (sseClient) {
        sseClient.close()
      }
      router.push('/')
    }

    // 发送消息
    const sendMessage = async () => {
      if (!inputMessage.value.trim() || isLoading.value) return

      const userMessage = {
        id: Date.now(),
        type: 'user',
        content: inputMessage.value,
        timestamp: new Date()
      }

      messages.value.push(userMessage)
      const currentMessage = inputMessage.value
      inputMessage.value = ''
      isLoading.value = true

      // 滚动到底部
      await nextTick()
      scrollToBottom()

      // 连接SSE
      connectSSE(currentMessage)
    }

    // 连接SSE
    const connectSSE = (message) => {
      const url = `http://localhost:8123/api/ai/plan_app/chat/sse_emitter?message=${encodeURIComponent(message)}&chatId=${chatId.value}`
      
      // 为这次对话创建唯一的AI消息ID
      const currentAiMessageId = `ai_${Date.now()}`
      
      sseClient = new SSEClient(url)
        .on('open', () => {
          console.log('SSE连接已建立')
        })
        .on('message', (data) => {
          // 处理接收到的数据
          if (data && data.trim()) {
            // 检查是否已有当前对话的AI消息，如果没有则创建
            let aiMessage = messages.value.find(msg => msg.type === 'ai' && msg.id === currentAiMessageId)
            
            if (!aiMessage) {
              aiMessage = {
                id: currentAiMessageId,
                type: 'ai',
                content: '',
                timestamp: new Date()
              }
              messages.value.push(aiMessage)
            }
            
            // 追加内容
            aiMessage.content += data
            
            // 滚动到底部
            nextTick(() => {
              scrollToBottom()
            })
          }
        })
        .on('error', (error) => {
          console.error('SSE错误:', error)
          isLoading.value = false
          
          // 检查是否已经有当前对话的AI消息，如果有则不显示错误消息
          const hasAiMessage = messages.value.some(msg => msg.type === 'ai' && msg.id === currentAiMessageId)
          
          if (!hasAiMessage) {
            // 如果连接出错且没有AI消息，添加错误消息
            const errorMessage = {
              id: `error_${Date.now()}`,
              type: 'ai',
              content: '抱歉，连接出现错误，请稍后重试。',
              timestamp: new Date()
            }
            messages.value.push(errorMessage)
          }
          
          // 清理连接
          if (sseClient) {
            sseClient.close()
            sseClient = null
          }
        })
        .on('close', () => {
          console.log('SSE连接已关闭')
          isLoading.value = false
          
          // 连接关闭时不需要修改ID，因为每个对话已经有唯一的ID
          
          // 清理连接
          if (sseClient) {
            sseClient.close()
            sseClient = null
          }
        })
        .connect()

      // 设置超时保护，防止连接一直挂起
      const timeoutId = setTimeout(() => {
        if (sseClient && isLoading.value) {
          console.log('SSE连接超时，主动关闭')
          isLoading.value = false
          
          // 检查是否有当前对话的AI消息
          const currentAiMessage = messages.value.find(msg => msg.id === currentAiMessageId)
          if (!currentAiMessage) {
            // 如果没有AI消息，添加超时提示
            const timeoutMessage = {
              id: `timeout_${Date.now()}`,
              type: 'ai',
              content: '连接超时，请重试。',
              timestamp: new Date()
            }
            messages.value.push(timeoutMessage)
          }
          
          if (sseClient) {
            sseClient.close()
            sseClient = null
          }
        }
      }, 30000) // 30秒超时

      // 监听连接关闭，清除超时定时器
      if (sseClient) {
        const originalClose = sseClient.close
        sseClient.close = function() {
          clearTimeout(timeoutId)
          originalClose.call(this)
        }
      }
    }

    // 滚动到底部
    const scrollToBottom = () => {
      if (messagesContainer.value) {
        messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
      }
    }

    // 格式化消息内容 - 支持markdown渲染
    const formatMessage = (content) => {
      if (!content) return ''
      
      // 配置marked选项
      marked.setOptions({
        breaks: true, // 支持换行符转换为<br>
        gfm: true,    // 启用GitHub风格的markdown
        sanitize: false // 允许HTML标签（注意：在生产环境中可能需要更严格的配置）
      })
      
      try {
        // 使用marked解析markdown
        return marked.parse(content)
      } catch (error) {
        console.error('Markdown解析错误:', error)
        // 如果解析失败，回退到简单的换行符替换
        return content.replace(/\n/g, '<br>')
      }
    }

    // 格式化时间
    const formatTime = (timestamp) => {
      return timestamp.toLocaleTimeString('zh-CN', { 
        hour: '2-digit', 
        minute: '2-digit' 
      })
    }

    onMounted(() => {
      initChatId()
      // 添加欢迎消息
      const welcomeMessage = {
        id: Date.now(),
        type: 'ai',
        content: `# 您好！我是AI计划大师 🎯

我专门帮助您制定各种计划和安排。我可以为您提供：

## 服务内容
- **旅行计划** - 详细的行程安排和景点推荐
- **学习计划** - 个性化的学习路径和时间安排  
- **工作计划** - 项目管理和任务分解
- **生活计划** - 日常安排和习惯养成

## 特色功能
✅ 智能分析您的需求  
✅ 提供详细的执行步骤  
✅ 考虑时间、预算等约束条件  
✅ 支持计划调整和优化  

请告诉我您需要什么样的计划，我会为您提供专业的建议和详细的规划方案！`,
        timestamp: new Date()
      }
      messages.value.push(welcomeMessage)
    })

    onUnmounted(() => {
      if (sseClient) {
        sseClient.close()
      }
    })

    return {
      messages,
      inputMessage,
      isLoading,
      chatId,
      messagesContainer,
      goBack,
      sendMessage,
      formatMessage,
      formatTime
    }
  }
}
</script>

<style scoped>
.chat-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.chat-header {
  background: white;
  padding: 16px 20px;
  border-bottom: 1px solid #e1e5e9;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.back-btn {
  padding: 8px 16px;
  font-size: 14px;
}

.chat-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.chat-id {
  font-size: 0.875rem;
  color: #666;
  background: #f8f9fa;
  padding: 4px 8px;
  border-radius: 4px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message {
  display: flex;
  max-width: 80%;
}

.message.user {
  align-self: flex-end;
}

.message.ai {
  align-self: flex-start;
}

.message-content {
  background: white;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: relative;
}

.message.user .message-content {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.message-text {
  line-height: 1.6;
  word-wrap: break-word;
}

/* Markdown样式 */
.message-text :deep(h1),
.message-text :deep(h2),
.message-text :deep(h3),
.message-text :deep(h4),
.message-text :deep(h5),
.message-text :deep(h6) {
  margin: 16px 0 8px 0;
  font-weight: 600;
  color: inherit;
}

.message-text :deep(h1) { font-size: 1.5em; }
.message-text :deep(h2) { font-size: 1.3em; }
.message-text :deep(h3) { font-size: 1.2em; }
.message-text :deep(h4) { font-size: 1.1em; }
.message-text :deep(h5) { font-size: 1.05em; }
.message-text :deep(h6) { font-size: 1em; }

.message-text :deep(p) {
  margin: 8px 0;
}

.message-text :deep(ul),
.message-text :deep(ol) {
  margin: 8px 0;
  padding-left: 24px;
}

.message-text :deep(li) {
  margin: 4px 0;
}

.message-text :deep(blockquote) {
  margin: 12px 0;
  padding: 8px 16px;
  border-left: 4px solid #ddd;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 0 4px 4px 0;
}

.message-text :deep(code) {
  background: rgba(0, 0, 0, 0.1);
  padding: 2px 6px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
  font-size: 0.9em;
}

.message-text :deep(pre) {
  background: rgba(0, 0, 0, 0.05);
  padding: 12px;
  border-radius: 6px;
  overflow-x: auto;
  margin: 12px 0;
}

.message-text :deep(pre code) {
  background: none;
  padding: 0;
  border-radius: 0;
}

.message-text :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin: 12px 0;
}

.message-text :deep(th),
.message-text :deep(td) {
  border: 1px solid #ddd;
  padding: 8px 12px;
  text-align: left;
}

.message-text :deep(th) {
  background: rgba(0, 0, 0, 0.05);
  font-weight: 600;
}

.message-text :deep(strong) {
  font-weight: 600;
}

.message-text :deep(em) {
  font-style: italic;
}

.message-text :deep(a) {
  color: #667eea;
  text-decoration: underline;
}

.message-text :deep(a:hover) {
  color: #764ba2;
}

.message-text :deep(hr) {
  border: none;
  border-top: 1px solid #ddd;
  margin: 16px 0;
}

.message-time {
  font-size: 0.75rem;
  opacity: 0.7;
  margin-top: 8px;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  align-items: center;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #999;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(1) {
  animation-delay: -0.32s;
}

.typing-indicator span:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes typing {
  0%, 80%, 100% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

.chat-input-container {
  background: white;
  border-top: 1px solid #e1e5e9;
  padding: 20px;
}

.input-wrapper {
  display: flex;
  gap: 12px;
  align-items: flex-end;
  max-width: 800px;
  margin: 0 auto;
}

.chat-input {
  flex: 1;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 12px;
  font-size: 16px;
  resize: none;
  outline: none;
  transition: border-color 0.3s ease;
}

.chat-input:focus {
  border-color: #667eea;
}

.chat-input:disabled {
  background: #f8f9fa;
  cursor: not-allowed;
}

.send-btn {
  padding: 12px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

@media (max-width: 768px) {
  .chat-header {
    padding: 12px 16px;
  }
  
  .chat-title {
    font-size: 1.25rem;
  }
  
  .chat-id {
    display: none;
  }
  
  .chat-messages {
    padding: 16px;
  }
  
  .message {
    max-width: 90%;
  }
  
  .input-wrapper {
    flex-direction: column;
    gap: 8px;
  }
  
  .send-btn {
    width: 100%;
  }
}
</style>
