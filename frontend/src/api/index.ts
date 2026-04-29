import axios from 'axios'
import { message, Modal } from 'ant-design-vue'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

api.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    // Handle 401 Unauthorized - redirect to login
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
      return Promise.reject(error)
    }

    // Handle 409 Conflict - optimistic locking
    if (error.response?.status === 409) {
      const data = error.response.data
      const conflictMessage = data?.message || 'This record has been modified by another user.'

      Modal.warning({
        title: 'Conflict Detected',
        content: `${conflictMessage} Please refresh the page and try again.`,
        okText: 'Refresh',
        onOk: () => {
          window.location.reload()
        },
      })

      return Promise.reject(error)
    }

    // Handle 500 Internal Server Error
    if (error.response?.status === 500) {
      message.error('An internal server error occurred. Please try again later.')
      return Promise.reject(error)
    }

    // Handle network errors
    if (!error.response) {
      message.error('Network error. Please check your connection and try again.')
      return Promise.reject(error)
    }

    return Promise.reject(error)
  }
)

export default api
