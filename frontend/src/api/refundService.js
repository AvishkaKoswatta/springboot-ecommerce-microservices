// import api from './index'

// export const refundService = {

//   // Request a refund (customer)
//   requestRefund: async (orderId, refundData) => {
//     try {
//       const response = await api.post(
//         `/orders/${orderId}/refund`,
//         refundData
//       )

//       return response.data.data
//     } catch (error) {
//       throw error.response?.data || error.message
//     }
//   },

//   // Get refund for an order (customer)
//   getRefundByOrderId: async (orderId) => {
//     try {
//       const response = await api.get(
//         `/orders/${orderId}/refund`
//       )

//       return response.data.data
//     } catch (error) {
//       throw error.response?.data || error.message
//     }
//   },

//   // Get pending refunds (ADMIN)
//   getPendingRefunds: async (page = 0, size = 10) => {
//     try {
//       const response = await api.get(
//         '/admin/refunds/pending',
//         { params: { page, size } }
//       )

//       return response.data.data
//     } catch (error) {
//       throw error.response?.data || error.message
//     }
//   },

//   // Resolve refund (ADMIN)
//   resolveRefund: async (id, refundResolution) => {
//     try {
//       const response = await api.patch(
//         `/admin/refunds/${id}/resolve`,
//         refundResolution
//       )

//       return response.data.data
//     } catch (error) {
//       throw error.response?.data || error.message
//     }
//   }
// }


import api from './index'

export const refundService = {
  // Request a refund (customer)
  // Request a refund (customer) — POST /payments/refund
requestRefund: async (refundRequest) => {
  try {
    const response = await api.post('/payments/refund', refundRequest)
    return response.data.data
  } catch (error) {
    throw error.response?.data || error.message
  }
},

  // Get refund for an order (customer)
  // getRefundByOrderId: async (orderId) => {
  //   try {
  //     const response = await api.get(`/orders/${orderId}/refund`)
  //     return response.data.data
  //   } catch (error) {
  //     throw error.response?.data || error.message
  //   }
  // },

  processRefund: async (refundReference) => {
  try {
    const response = await api.post(
      `/admin/payments/refund/${refundReference}/process`
    )
    return response.data.data
  } catch (error) {
    throw error.response?.data || error.message
  }
},

  // Get all refunds with optional status filter (ADMIN)
  getAllRefunds: async (page = 0, size = 10, status = null) => {
    try {
      const params = { page, size }
      if (status) params.status = status
      const response = await api.get('/admin/payments/refunds', { params })
      return response.data.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  }

  

  
}