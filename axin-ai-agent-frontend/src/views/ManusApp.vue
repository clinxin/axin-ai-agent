<template>
  <div class="chat-container">
    <div class="chat-header">
      <button class="btn btn-secondary back-btn" @click="goBack">
        ← 返回主页
      </button>
      <h1 class="chat-title">智多星AI智能体</h1>
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
          placeholder="请输入您的问题或需求..."
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
  name: 'ManusApp',
  setup() {
    const router = useRouter()
    const messages = ref([])
    const inputMessage = ref('')
    const isLoading = ref(false)
    const chatId = ref('')
    const messagesContainer = ref(null)
    const isTaskCompleted = ref(false) // 添加任务完成状态
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

      // 如果任务已完成，重置状态并允许新对话
      if (isTaskCompleted.value) {
        isTaskCompleted.value = false
        messages.value = [] // 清空消息历史，开始新对话
        // 确保关闭之前的连接
        if (sseClient) {
          sseClient.close()
          sseClient = null
        }
      }

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
      // 先关闭之前的连接
      if (sseClient) {
        sseClient.close()
        sseClient = null
      }

      const url = `http://localhost:8123/api/ai/manus/chat?message=${encodeURIComponent(message)}&chatId=${chatId.value}`
      
      sseClient = new SSEClient(url)
        .on('open', () => {
          console.log('SSE连接已建立')
        })
        .on('message', (data) => {
          // 如果任务已完成，忽略所有后续消息
          if (isTaskCompleted.value) {
            console.log('任务已完成，忽略后续消息:', data)
            return
          }
          
          // 处理接收到的数据
          if (data && data.trim()) {
            // 检查是否包含任务结束的信号
            const isTaskFinished = data.includes('任务结束') || 
                                  data.includes('doTerminate') || 
                                  data.includes('执行结束') ||
                                  data.includes('Terminated') ||
                                  data.includes('工具 doTerminate 返回的结果') ||
                                  data.includes('达到最大步骤')
            
            // 如果检测到任务结束，立即关闭SSE连接并返回
            if (isTaskFinished) {
              console.log('检测到任务结束信号，立即关闭SSE连接')
              isLoading.value = false
              isTaskCompleted.value = true // 设置任务完成状态
              
              // 添加任务结束提示消息
              const finishMessage = {
                id: `finish_${Date.now()}`,
                type: 'ai',
                content: '✅ 任务已完成，对话结束。您可以继续提出新的问题。',
                timestamp: new Date()
              }
              messages.value.push(finishMessage)
              
              // 立即关闭SSE连接
              if (sseClient) {
                sseClient.close()
                sseClient = null
              }
              
              // 滚动到底部并返回，不处理后续消息
              nextTick(() => {
                scrollToBottom()
              })
              return
            }
            
            // 智多星AI智能体：每个步骤使用独立的气泡
            // 为每个步骤创建新的AI消息
            const stepMessageId = `step_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
            const stepMessage = {
              id: stepMessageId,
              type: 'ai',
              content: data,
              timestamp: new Date()
            }
            messages.value.push(stepMessage)
            
            // 滚动到底部
            nextTick(() => {
              scrollToBottom()
            })
          }
        })
        .on('error', (error) => {
          console.error('SSE错误:', error)
          isLoading.value = false
          
          // 如果任务已完成，不处理错误消息
          if (isTaskCompleted.value) {
            console.log('任务已完成，忽略SSE错误')
            return
          }
          
          // 检查是否已经有当前对话的AI消息，如果有则不显示错误消息
          const hasAiMessage = messages.value.some(msg => msg.type === 'ai' && msg.id.startsWith('step_'))
          
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
          
          // 如果任务已完成，不处理连接关闭事件
          if (isTaskCompleted.value) {
            console.log('任务已完成，忽略连接关闭事件')
            return
          }
          
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
        if (sseClient && isLoading.value && !isTaskCompleted.value) {
          console.log('SSE连接超时，主动关闭')
          isLoading.value = false
          
          // 检查是否有当前对话的AI消息
          const hasAiMessage = messages.value.some(msg => msg.type === 'ai' && msg.id.startsWith('step_'))
          if (!hasAiMessage) {
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

    // 格式化消息内容
    const formatMessage = (content) => {
      try {
        // 使用marked解析markdown
        return marked.parse(content)
      } catch (error) {
        console.error('Markdown解析错误:', error)
        // 如果解析失败，回退到简单的换行处理
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
        content: '您好！我是智多星AI智能体，一个全能的AI助手。我可以回答各种问题，提供智能建议，帮助您解决各种难题。请告诉我您需要什么帮助！',
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
.message-text h1, .message-text h2, .message-text h3, 
.message-text h4, .message-text h5, .message-text h6 {
  margin: 12px 0 8px 0;
  font-weight: 600;
  line-height: 1.3;
}

.message-text h1 { font-size: 1.4em; }
.message-text h2 { font-size: 1.3em; }
.message-text h3 { font-size: 1.2em; }
.message-text h4 { font-size: 1.1em; }
.message-text h5 { font-size: 1.05em; }
.message-text h6 { font-size: 1em; }

.message-text p {
  margin: 8px 0;
}

.message-text ul, .message-text ol {
  margin: 8px 0;
  padding-left: 20px;
}

.message-text li {
  margin: 4px 0;
}

.message-text blockquote {
  margin: 12px 0;
  padding: 8px 12px;
  border-left: 3px solid #667eea;
  background: rgba(102, 126, 234, 0.05);
  border-radius: 0 4px 4px 0;
}

.message-text code {
  background: rgba(0, 0, 0, 0.08);
  padding: 2px 4px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
  font-size: 0.9em;
}

.message-text pre {
  background: rgba(0, 0, 0, 0.05);
  padding: 10px;
  border-radius: 6px;
  overflow-x: auto;
  margin: 10px 0;
}

.message-text pre code {
  background: none;
  padding: 0;
  border-radius: 0;
}

.message-text table {
  border-collapse: collapse;
  width: 100%;
  margin: 10px 0;
}

.message-text th, .message-text td {
  border: 1px solid #ddd;
  padding: 6px 10px;
  text-align: left;
}

.message-text th {
  background: rgba(102, 126, 234, 0.1);
  font-weight: 600;
}

.message-text a {
  color: #667eea;
  text-decoration: none;
}

.message-text a:hover {
  text-decoration: underline;
}

.message-text hr {
  border: none;
  border-top: 1px solid #ddd;
  margin: 12px 0;
}

/* 用户消息中的markdown样式调整 */
.message.user .message-text h1, .message.user .message-text h2, 
.message.user .message-text h3, .message.user .message-text h4, 
.message.user .message-text h5, .message.user .message-text h6 {
  color: white;
}

.message.user .message-text blockquote {
  border-left-color: rgba(255, 255, 255, 0.5);
  background: rgba(255, 255, 255, 0.1);
}

.message.user .message-text code {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

.message.user .message-text pre {
  background: rgba(255, 255, 255, 0.1);
}

.message.user .message-text th {
  background: rgba(255, 255, 255, 0.1);
}

.message.user .message-text a {
  color: rgba(255, 255, 255, 0.9);
}

.message.user .message-text hr {
  border-top-color: rgba(255, 255, 255, 0.3);
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
