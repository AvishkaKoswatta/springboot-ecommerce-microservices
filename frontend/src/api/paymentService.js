import api from './index'

export const paymentService = {

  // Initiate a payment for an order (called right after order creation for online payments)
  // POST /payments/initiate
  // Body: { orderId, orderNumber, amount, currency, paymentMethod }
  initiatePayment: async (paymentData) => {
    try {
      const response = await api.post('/payments/initiate', paymentData)
      return response.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },

  // Process / confirm a payment (submit card details or UPI/NetBanking credentials)
  // POST /payments/process
  // Body: { paymentReference, cardNumber, expiryMonth, expiryYear, cvv, cardHolderName }
  processPayment: async (paymentData) => {
    try {
      const response = await api.post('/payments/process', paymentData)
      return response.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },

  // Get payment status for a specific order
  // GET /payments/order/{orderId}
  getPaymentByOrderId: async (orderId) => {
    try {
      const response = await api.get(`/payments/order/${orderId}`)
      return response.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },

  // Get payment by payment reference string
  // GET /payments/reference/{paymentReference}
  getPaymentByReference: async (paymentReference) => {
    try {
      const response = await api.get(`/payments/reference/${paymentReference}`)
      return response.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },

  // Get current user's payment history (paginated)
  // GET /payments/my-payments?page=0&size=10
  getMyPayments: async (page = 0, size = 10) => {
    try {
      const response = await api.get('/payments/my-payments', {
        params: { page, size }
      })
      return response.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },
}
