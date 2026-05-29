import React from 'react'
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { Toaster } from 'react-hot-toast'
import { AuthProvider } from './context/AuthContext'
import { CartProvider } from './context/CartContext'
import ProtectedRoute from './components/ProtectedRoute'

// Layouts
import UserLayout from './layouts/UserLayout'
import AdminLayout from './layouts/AdminLayout'

// Auth Pages
import LoginPage from './pages/auth/LoginPage'
import RegisterPage from './pages/auth/RegisterPage'
import ForgotPasswordPage from './pages/auth/ForgotPasswordPage'
import ResetPasswordPage from './pages/auth/ResetPasswordPage'

// User Pages
import HomePage from './pages/user/HomePage'
import ProductDetailsPage from './pages/user/ProductDetailsPage'
import CartPage from './pages/user/CartPage'
import CheckoutPage from './pages/user/CheckoutPage'           // ← updated
import MyOrdersPage from './pages/user/MyOrdersPage'
import OrderDetailsPage from './pages/user/OrderDetailsPage'   // ← updated
import RefundRequestPage from './pages/user/RefundRequestPage'
import ProfilePage from './pages/user/ProfilePage'
import SettingsPage from './pages/user/SettingsPage'

// ── NEW: Payment Pages ──────────────────────────────────────────────────
import PaymentPage from './pages/user/PaymentPage'
import PaymentSuccessPage from './pages/user/PaymentSuccessPage'
import PaymentHistoryPage from './pages/user/PaymentHistoryPage'
// ────────────────────────────────────────────────────────────────────────

// Admin Pages
import AdminDashboardPage from './pages/admin/AdminDashboardPage'
import AdminProductsPage from './pages/admin/AdminProductsPage'
import AdminOrdersPage from './pages/admin/AdminOrdersPage'
import AdminRefundsPage from './pages/admin/AdminRefundsPage'
import AdminUsersPage from './pages/admin/AdminUsersPage'
import AdminCategoriesPage from './pages/admin/AdminCategoriesPage'
import AdminReviewsPage from './pages/admin/AdminReviewsPage'

import VerifyEmailPage from './pages/email/VerifyEmailPage'

export default function App() {
  return (
    <Router>
      <AuthProvider>
        <CartProvider>
          <Routes>
            {/* Public Routes */}
            <Route path="/login"    element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/auth/verify-email" element={<VerifyEmailPage />} />
            <Route path="/forgot-password"   element={<ForgotPasswordPage />} />
            <Route path="/auth/reset-password"    element={<ResetPasswordPage />} />

            {/* Protected User Routes */}
            <Route
              element={
                <ProtectedRoute>
                  <UserLayout />
                </ProtectedRoute>
              }
            >
              <Route path="/"                        element={<HomePage />} />
              <Route path="/product/:id"             element={<ProductDetailsPage />} />
              <Route path="/cart"                    element={<CartPage />} />
              <Route path="/checkout"                element={<CheckoutPage />} />
              <Route path="/orders"                  element={<MyOrdersPage />} />
              <Route path="/order/:id"               element={<OrderDetailsPage />} />
              <Route path="/refund-request/:id"      element={<RefundRequestPage />} />
              <Route path="/profile"                 element={<ProfilePage />} />
              <Route path="/settings"                element={<SettingsPage />} />

              {/* ── Payment Routes ── */}
              <Route path="/payment/:orderId"         element={<PaymentPage />} />
              <Route path="/payment-success/:orderId" element={<PaymentSuccessPage />} />
              <Route path="/payment-history"          element={<PaymentHistoryPage />} />
            </Route>

            {/* Protected Admin Routes */}
            <Route
              element={
                <ProtectedRoute requiredRole="ADMIN">
                  <AdminLayout />
                </ProtectedRoute>
              }
            >
              <Route path="/admin/dashboard"  element={<AdminDashboardPage />} />
              <Route path="/admin/products"   element={<AdminProductsPage />} />
              <Route path="/admin/orders"     element={<AdminOrdersPage />} />
              <Route path="/admin/refunds"    element={<AdminRefundsPage />} />
              <Route path="/admin/users"      element={<AdminUsersPage />} />
              <Route path="/admin/categories" element={<AdminCategoriesPage />} />
              <Route path="/admin/reviews"    element={<AdminReviewsPage />} />
            </Route>

            {/* Catch-all */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>

          <Toaster position="top-right" reverseOrder={false} />
        </CartProvider>
      </AuthProvider>
    </Router>
  )
}
