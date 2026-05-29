import api from './index'

export const orderService = {
  // Create a new order
  createOrder: async (orderData) => {
    try {
      const response = await api.post('/orders', orderData)
      return response.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },

  // Get user's orders
  getMyOrders: async (page = 0, size = 10) => {
    try {
      const response = await api.get('/orders/my-orders', {
        params: { page, size }
      })
      return response.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },

  // Get order details by ID
  getOrderById: async (id) => {
    try {
      const response = await api.get(`/orders/${id}`)
      return response.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },

  // Get all orders (admin only)
 getAllOrders: async (page = 0, size = 10, status = '') => {
  try {
    const response = await api.get('/admin/orders', {
      params: { page, size, status }
    })

    return response.data.data   // ✅ return ONLY paged data
  } catch (error) {
    throw error.response?.data || error.message
  }
},

  // Update order status (admin only)
  updateOrderStatus: async (id, status) => {
    try {
      const response = await api.patch( `/admin/orders/${id}/status`, { status })
      return response.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  }
}
