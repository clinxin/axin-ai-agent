// SSE工具类
export class SSEClient {
  constructor(url, options = {}) {
    this.url = url
    this.options = options
    this.eventSource = null
    this.listeners = new Map()
  }

  // 连接SSE
  connect() {
    if (this.eventSource) {
      this.close()
    }

    this.eventSource = new EventSource(this.url)
    
    this.eventSource.onopen = () => {
      this.emit('open')
    }

    this.eventSource.onmessage = (event) => {
      this.emit('message', event.data)
    }

    this.eventSource.onerror = (error) => {
      this.emit('error', error)
    }

    // 监听连接关闭事件
    this.eventSource.addEventListener('close', () => {
      this.emit('close')
    })

    return this
  }

  // 监听事件
  on(event, callback) {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, [])
    }
    this.listeners.get(event).push(callback)
    return this
  }

  // 移除监听器
  off(event, callback) {
    if (this.listeners.has(event)) {
      const callbacks = this.listeners.get(event)
      const index = callbacks.indexOf(callback)
      if (index > -1) {
        callbacks.splice(index, 1)
      }
    }
    return this
  }

  // 触发事件
  emit(event, data) {
    if (this.listeners.has(event)) {
      this.listeners.get(event).forEach(callback => {
        callback(data)
      })
    }
  }

  // 关闭连接
  close() {
    if (this.eventSource) {
      this.eventSource.close()
      this.eventSource = null
    }
    return this
  }

  // 获取连接状态
  get readyState() {
    return this.eventSource ? this.eventSource.readyState : EventSource.CLOSED
  }
}

// 生成聊天室ID
export function generateChatId() {
  return 'chat_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
}
