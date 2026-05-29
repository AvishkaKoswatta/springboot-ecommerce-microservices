import React, { useState, useEffect } from 'react'
import { Container, Row, Col, Card, Spinner } from 'react-bootstrap'
import { orderService } from '../../api/orderService'
import { productService } from '../../api/productService'
import { refundService } from '../../api/refundService'
import { userService } from '../../api/userService'

import {
  Package,
  ShoppingBag,
  Users,
  RefreshCw,
  LayoutDashboard,
  ClipboardList,
  CreditCard,
  User
} from 'lucide-react'

export const AdminDashboardPage = () => {
  const [stats, setStats] = useState({
    totalOrders: 0,
    totalProducts: 0,
    pendingRefunds: 0,
    totalUsers: 0
  })

  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    fetchStats()
  }, [])

  const fetchStats = async () => {
    setIsLoading(true)

    try {
      const [orders, products, refunds, users] =
        await Promise.allSettled([
          orderService.getAllOrders(0, 1),
          productService.getAllProducts(0, 1),
          refundService.getAllRefunds(0, 1),
          userService.getAllUsers(0, 1)
        ])

      setStats({
        totalOrders:
          orders.status === 'fulfilled'
            ? orders.value.totalElements
            : 0,

        totalProducts:
          products.status === 'fulfilled'
            ? products.value.totalElements
            : 0,

        pendingRefunds:
          refunds.status === 'fulfilled'
            ? refunds.value.totalElements
            : 0,

        totalUsers:
          users.status === 'fulfilled'
            ? users.value.totalElements
            : 0
      })
    } finally {
      setIsLoading(false)
    }
  }

  const StatCard = ({ title, value, icon, color }) => (
    <Card className="border-0 shadow-sm h-100 stat-card">
      <Card.Body className="d-flex align-items-center gap-3">

        <div
          className="d-flex align-items-center justify-content-center rounded"
          style={{
            width: 52,
            height: 52,
            background: `${color}15`
          }}
        >
          <div style={{ color }}>{icon}</div>
        </div>

        <div>
          <div className="text-muted small">{title}</div>
          <div className="fs-4 fw-bold">
            {isLoading ? <Spinner size="sm" /> : value}
          </div>
        </div>

      </Card.Body>

      <style>{`
        .stat-card {
          transition: transform 0.2s ease, box-shadow 0.2s ease;
        }
        .stat-card:hover {
          transform: translateY(-3px);
          box-shadow: 0 10px 25px rgba(0,0,0,0.08);
        }
      `}</style>
    </Card>
  )

  return (
    <div style={{ background: '#f6f7fb', minHeight: '100vh' }}>
      <Container fluid className="py-4">

        {/* HEADER */}
        <div className="mb-4">
          <h2 className="fw-bold d-flex align-items-center gap-2">
            <LayoutDashboard /> Admin Dashboard
          </h2>
          <small className="text-muted">
            System overview & analytics
          </small>
        </div>

        {/* STATS */}
        <Row className="g-3">

          <Col md={6} lg={3}>
            <StatCard
              title="Total Orders"
              value={stats.totalOrders}
              icon={<Package size={22} />}
              color="#0d6efd"
            />
          </Col>

          <Col md={6} lg={3}>
            <StatCard
              title="Total Products"
              value={stats.totalProducts}
              icon={<ShoppingBag size={22} />}
              color="#198754"
            />
          </Col>

          <Col md={6} lg={3}>
            <StatCard
              title="Pending Refunds"
              value={stats.pendingRefunds}
              icon={<RefreshCw size={22} />}
              color="#ffc107"
            />
          </Col>

          <Col md={6} lg={3}>
            <StatCard
              title="Total Users"
              value={stats.totalUsers}
              icon={<Users size={22} />}
              color="#17a2b8"
            />
          </Col>

        </Row>

        {/* QUICK ACTIONS */}
        <Row className="mt-4">
          <Col>
            <Card className="border-0 shadow-sm">
              <Card.Body>

                <h5 className="fw-bold mb-3">
                  Quick Actions
                </h5>

                <Row className="g-3">

                  <Col md={3}>
                    <a href="/admin/products" className="btn btn-outline-primary w-100 d-flex align-items-center justify-content-center gap-2">
                      <ShoppingBag size={16} />
                      Products
                    </a>
                  </Col>

                  <Col md={3}>
                    <a href="/admin/orders" className="btn btn-outline-primary w-100 d-flex align-items-center justify-content-center gap-2">
                      <ClipboardList size={16} />
                      Orders
                    </a>
                  </Col>

                  <Col md={3}>
                    <a href="/admin/refunds" className="btn btn-outline-warning w-100 d-flex align-items-center justify-content-center gap-2">
                      <CreditCard size={16} />
                      Refunds
                    </a>
                  </Col>

                  <Col md={3}>
                    <a href="/admin/users" className="btn btn-outline-info w-100 d-flex align-items-center justify-content-center gap-2">
                      <User size={16} />
                      Users
                    </a>
                  </Col>

                </Row>

              </Card.Body>
            </Card>
          </Col>
        </Row>

      </Container>
    </div>
  )
}

export default AdminDashboardPage