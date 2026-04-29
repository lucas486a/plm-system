import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Antd from 'ant-design-vue'
import App from './App.vue'
import router from './router'
import 'ant-design-vue/dist/reset.css'

const app = createApp(App)

// Global error handler for Vue errors
app.config.errorHandler = (err, instance, info) => {
  console.error('Vue error:', err)
  console.error('Component:', instance)
  console.error('Info:', info)
}

// Global warning handler (development only)
if (import.meta.env.DEV) {
  app.config.warnHandler = (msg, instance, trace) => {
    console.warn('Vue warning:', msg)
    console.warn('Component:', instance)
    console.warn('Trace:', trace)
  }
}

app.use(createPinia())
app.use(router)
app.use(Antd)

app.mount('#app')

// Handle unhandled promise rejections
window.addEventListener('unhandledrejection', (event) => {
  console.error('Unhandled promise rejection:', event.reason)
})

// Handle uncaught errors
window.addEventListener('error', (event) => {
  console.error('Uncaught error:', event.error)
})
