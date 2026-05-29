import React, { createContext, useContext, useState } from 'react'

const AuthContext = createContext()

export const AuthProvider = ({ children }) => {

  // ✅ Load from localStorage immediately (no flicker, no re-login)
  const [user, setUser] = useState(() => {
    try {
      const storedUser = localStorage.getItem('user')
      return storedUser ? JSON.parse(storedUser) : null
    } catch (e) {
      localStorage.removeItem('user')
      return null
    }
  })

  const [accessToken, setAccessToken] = useState(() => {
    return localStorage.getItem('accessToken') || null
  })

  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState(null)

  const login = (userData, token) => {
    setAccessToken(token)
    setUser(userData)

    localStorage.setItem('accessToken', token)
    localStorage.setItem('user', JSON.stringify(userData))
  }

  const logout = () => {
    setAccessToken(null)
    setUser(null)
    localStorage.removeItem('accessToken')
    localStorage.removeItem('user')
  }

  const isAdmin = () => {
    return user?.roles?.includes('ROLE_ADMIN') || false
  }

  const isAuthenticated = () => {
    return !!accessToken && !!user
  }

  return (
    <AuthContext.Provider
      value={{
        user,
        accessToken,
        isLoading,
        error,
        login,
        logout,
        isAdmin,
        isAuthenticated,
        setUser,
        setIsLoading,
        setError
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return context
}