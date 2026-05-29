import api from './index'

export const productService = {

  getAllProducts: async (page = 0, size = 12, search = '') => {
    try {
      const response = await api.get('/products', {
        params: { page, size, search }
      })
      return response.data.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },

  searchProducts: async (params = {}) => {
    try {
      const response = await api.get('/products/search', {
        params: {
          query:       params.query       || undefined,
          categoryIds: params.categoryIds || undefined,
          brand:       params.brand       || undefined,
          minPrice:    params.minPrice    ?? undefined,
          maxPrice:    params.maxPrice    ?? undefined,
          page:        params.page        ?? 0,
          size:        params.size        ?? 12,
          sortBy:      params.sortBy      || 'createdAt',
          sortDir:     params.sortDir     || 'desc',
        },
        // Axios serializes arrays as: categoryIds=1&categoryIds=4&categoryIds=8
        // which Spring binds correctly to List<Long> categoryIds
        paramsSerializer: (p) => {
          const sp = new URLSearchParams()
          Object.entries(p).forEach(([key, val]) => {
            if (val === undefined || val === null) return
            if (Array.isArray(val)) {
              val.forEach(v => sp.append(key, v))
            } else {
              sp.append(key, val)
            }
          })
          return sp.toString()
        }
      })
      return response.data.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },

  getFeaturedProducts: async () => {
    try {
      const response = await api.get('/products/featured')
      return response.data.data || []
    } catch (error) {
      throw error.response?.data || error.message
    }
  },

  getAllBrands: async () => {
    try {
      const response = await api.get('/products/brands')
      return response.data.data || []
    } catch (error) {
      throw error.response?.data || error.message
    }
  },

  getProductById: async (id) => {
    try {
      const response = await api.get(`/products/${id}`)
      return response.data.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },

  createProduct: async (productData) => {
    try {
      const response = await api.post('/products', productData)
      return response.data.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },

  updateProduct: async (id, productData) => {
    try {
      const response = await api.put(`/products/${id}`, productData)
      return response.data.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },

  deleteProduct: async (id) => {
    try {
      const response = await api.delete(`/products/${id}`)
      return response.data.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },

  uploadImage: async (file) => {
    try {
      const formData = new FormData()
      formData.append('file', file)
      const response = await api.post('/files/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
      return response.data.data || response.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },
}