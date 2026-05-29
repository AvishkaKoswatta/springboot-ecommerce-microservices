import api from './index'

export const reviewService = {

  // Create review
  createReview: async (productId, reviewData) => {
    try {
      const res = await api.post(
        `/products/${productId}/reviews`,
        reviewData
      )
      return res.data.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },

  // Public reviews
  getReviews: async (productId, page = 0, size = 10) => {
    try {
      const res = await api.get(
        `/products/${productId}/reviews`,
        { params: { page, size } }
      )
      return res.data.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },

  // Summary
  getReviewSummary: async (productId) => {
    try {
      const res = await api.get(
        `/products/${productId}/reviews/summary`
      )
      return res.data.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },

  // Admin: all reviews (FIXED SINGLE VERSION)
  getAllReviews: async (productId, page = 0, size = 10) => {
    try {
      const res = await api.get(
        `/products/${productId}/reviews/all`,
        { params: { page, size } }
      )
      return res.data.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },

  // Admin: pending reviews
  getPendingReviews: async (page = 0, size = 10) => {
    try {
      const res = await api.get(
        '/reviews/pending',
        { params: { page, size } }
      )
      return res.data.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },

  // Update status
  updateReviewStatus: async (reviewId, status) => {
    try {
      const res = await api.patch(
        `/reviews/${reviewId}/status`,
        { status }
      )
      return res.data.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },

  // Delete review
  deleteReview: async (reviewId) => {
    try {
      const res = await api.delete(
        `/reviews/${reviewId}`
      )
      return res.data.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  }
}