import React from 'react'
import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export const ProtectedRoute = ({ children, requiredRole = null }) => {
  const { isAuthenticated, isAdmin } = useAuth()

  if (!isAuthenticated()) {
    return <Navigate to="/login" replace />
  }

  // Check for role-based access
  if (requiredRole === 'ADMIN' && !isAdmin()) {
    return <Navigate to="/" replace />
  }

  return children
}

export default ProtectedRoute
