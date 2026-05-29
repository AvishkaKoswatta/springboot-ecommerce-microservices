import React from 'react'
import { Outlet } from 'react-router-dom'
import Header from '../components/Header'

export const UserLayout = () => {
  return (
    <div className="d-flex flex-column min-vh-100">
      <Header />
      <div className="flex-grow-1">
        <Outlet />
      </div>
      <footer className="bg-dark text-white py-4 mt-5">
        <div className="container text-center">
          <p className="mb-0">&copy; 2024 E-Commerce Store. All rights reserved.</p>
        </div>
      </footer>
    </div>
  )
}

export default UserLayout
