import api from './index'

export const categoryService = {

  getAllCategories: async () => {
    try {
      const response = await api.get('/categories')
      return response.data.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },

  getRootCategories: async () => {
    try {
      const response = await api.get('/categories/roots')
      return response.data.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },

  createCategory: async (data) => {
    try {
      const response = await api.post('/categories', data)
      return response.data.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },

  updateCategory: async (id, data) => {
    try {
      const response = await api.put(`/categories/${id}`, data)
      return response.data.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },

  deleteCategory: async (id) => {
    try {
      const response = await api.delete(`/categories/${id}`)
      return response.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  }

}