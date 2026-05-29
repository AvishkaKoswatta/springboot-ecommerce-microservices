import api from './index'

export const userService = {

  // Get current user profile
  getProfile: async () => {
    try {
      const response = await api.get('/users/me')
      return response.data.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },

  // Update profile fields (username, firstName, lastName, phoneNumber, profilePictureUrl)
  updateProfile: async (payload) => {
    try {
      const response = await api.put('/users/me', payload)
      return response.data.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },

  // Change password (authenticated user)
  changePassword: async (currentPassword, newPassword, confirmPassword) => {
    try {
      const response = await api.patch('/users/me/password', {
        currentPassword,
        newPassword,
        confirmPassword,
      })
      return response.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },

  // Get all users (admin only)
  getAllUsers: async (page = 0, size = 10, search = '') => {
    try {
      const response = await api.get('/admin/users', { params: { page, size, search } })
      return response.data.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },
}
