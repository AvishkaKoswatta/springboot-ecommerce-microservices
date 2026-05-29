import React, { createContext, useContext, useState, useEffect } from 'react'

const CartContext = createContext()

export const CartProvider = ({ children }) => {
  const [cart, setCart] = useState(() => {
  const storedCart = localStorage.getItem('cart')

  return storedCart
    ? JSON.parse(storedCart)
    : []
})
  const [total, setTotal] = useState(0)

  // Load cart from localStorage
  // useEffect(() => {
  //   const storedCart = localStorage.getItem('cart')

  //   if (storedCart) {
  //     const parsedCart = JSON.parse(storedCart)

  //     setCart(parsedCart)
  //     calculateTotal(parsedCart)
  //   }
  // }, [])

  // Save cart whenever updated
  useEffect(() => {
    localStorage.setItem('cart', JSON.stringify(cart))
    calculateTotal(cart)
  }, [cart])

  // Calculate cart total
  const calculateTotal = (cartItems) => {
    const totalAmount = cartItems.reduce(
      (sum, item) =>
        sum + ((item.price || 0) * (item.quantity || 0)),
      0
    )

    setTotal(totalAmount)
  }

  // Add item to cart
const addToCart = (product) => {
  setCart((prevCart) => {

    const addedQuantity = product.quantity || 1

    const existingItem = prevCart.find(
      (item) => item.id === product.id
    )

    const imageUrl =
      product.images?.length > 0
        ? `http://localhost:8079${product.images[0].url}`
        : null

    if (existingItem) {
      return prevCart.map((item) =>
        item.id === product.id
          ? {
              ...item,
              quantity: item.quantity + addedQuantity
            }
          : item
      )
    }

    return [
      ...prevCart,
      {
        id: product.id,
        name: product.name,
        price: product.price,
        quantity: addedQuantity,
        imageUrl: imageUrl   // 🔥 IMPORTANT FIX
      }
    ]
  })
}

  // Remove item
  const removeFromCart = (id) => {
    setCart((prevCart) =>
      prevCart.filter(
        (item) => item.id !== id
      )
    )
  }

  // Update quantity
  const updateQuantity = (id, quantity) => {

    if (quantity <= 0) {
      removeFromCart(id)
      return
    }

    setCart((prevCart) =>
      prevCart.map((item) =>
        item.id === id
          ? {
              ...item,
              quantity
            }
          : item
      )
    )
  }

  // Clear cart
  const clearCart = () => {
    setCart([])
    setTotal(0)
    localStorage.removeItem('cart')
  }

  // Total amount
  const getCartTotal = () => {
    return total.toFixed(2)
  }

  // Total quantity count
  const getCartCount = () => {
    return cart.reduce(
      (sum, item) => sum + item.quantity,
      0
    )
  }

  const value = {
    cart,
    total,
    addToCart,
    removeFromCart,
    updateQuantity,
    clearCart,
    getCartTotal,
    getCartCount
  }

  return (
    <CartContext.Provider value={value}>
      {children}
    </CartContext.Provider>
  )
}

export const useCart = () => {
  const context = useContext(CartContext)

  if (!context) {
    throw new Error(
      'useCart must be used within CartProvider'
    )
  }

  return context
}