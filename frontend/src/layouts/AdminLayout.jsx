import React from 'react'
import { Outlet, Navigate } from 'react-router-dom'
import { Container, Row, Col } from 'react-bootstrap'
import AdminSidebar from '../components/AdminSidebar'
import { useAuth } from '../context/AuthContext'

export const AdminLayout = () => {
  const { isAdmin, isAuthenticated } = useAuth()

  if (!isAuthenticated() || !isAdmin()) {
    return <Navigate to="/" replace />
  }

  return (
    <div className="d-flex min-vh-100">
      <div style={{ width: '250px' }} className="d-lg-block">
        <AdminSidebar />
      </div>
      <div className="flex-grow-1 overflow-auto">
        <div className="content-area">
          <Outlet />
        </div>
      </div>
    </div>
  )
}

export default AdminLayout
