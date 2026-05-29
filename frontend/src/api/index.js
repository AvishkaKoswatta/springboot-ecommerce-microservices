import axios from 'axios'

// Configure base API URL - adjust this based on your backend setup
const API_BASE_URL =
  import.meta.env.VITE_API_URL || 'http://api:8079'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor to add JWT token to headers
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor to handle 401 and refresh token logic
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Clear auth data on 401
      localStorage.removeItem('accessToken')
      localStorage.removeItem('user')
      // Optionally redirect to login
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default api
