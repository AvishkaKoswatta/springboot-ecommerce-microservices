import api from './index'

export const authService = {
  // Register a new user
  register: async (userData) => {
    try {
      const response = await api.post('/auth/register', userData)
      return response.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },

  // Login user
  login: async (credentials) => {
    try {
      const response = await api.post('/auth/login', credentials)
      return response.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },

  // Verify email token
  verifyEmail: async (token) => {
    try {
      const response = await api.get('/auth/verify-email', { params: { token } })
      return response.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },

  // Resend verification email
  resendVerificationEmail: async (email) => {
    try {
      const response = await api.post('/auth/resend-verification', null, { params: { email } })
      return response.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },

  // Request a password reset link
  forgotPassword: async (email) => {
    try {
      const response = await api.post('/auth/forgot-password', { email })
      return response.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },

  // Reset password using token from email
  resetPassword: async (token, newPassword) => {
    try {
      const response = await api.post('/auth/reset-password', { token, newPassword })
      return response.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },
}
