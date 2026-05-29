import React from 'react'
import { Container, Row, Col, Card, Badge, Button } from 'react-bootstrap'
import { useAuth } from '../../context/AuthContext'
import LoadingSpinner from '../../components/LoadingSpinner'
import { Link } from 'react-router-dom'
import {
  User, Mail, Shield, Calendar, CheckCircle, XCircle,
  Settings, ClipboardList, ShoppingCart, ShoppingBag, CreditCard
} from 'lucide-react'

export const ProfilePage = () => {
  const { user } = useAuth()

  if (!user) return <LoadingSpinner />

  const isAdmin = user.roles?.includes('ROLE_ADMIN')

  const statusColor = user.status === 'ACTIVE' ? '#198754' : '#fd7e14'
  const statusLabel = user.status || 'ACTIVE'

  return (
    <div style={{ background: '#f6f7fb', minHeight: '100vh' }}>
      <Container className="py-5">

        {/* Header */}
        <div className="d-flex justify-content-between align-items-start mb-4">
          <div>
            <h2 className="fw-bold mb-1 d-flex align-items-center gap-2">
              <User size={24} /> My Profile
            </h2>
            <p className="text-muted mb-0 small">View your account overview</p>
          </div>
          {/* <Link to="/settings" className="btn btn-outline-primary btn-sm d-flex align-items-center gap-1">
            <Settings size={15} /> Edit Settings
          </Link> */}
        </div>

        <Row className="g-4">

          {/* Avatar + Name Card */}
          <Col lg={4}>
            <Card className="border-0 shadow-sm text-center">
              <Card.Body className="py-4">
                <div
                  style={{
                    width: 90, height: 90, borderRadius: '50%',
                    background: isAdmin ? '#dc3545' : '#0d6efd',
                    color: 'white', display: 'flex',
                    alignItems: 'center', justifyContent: 'center',
                    fontSize: 36, fontWeight: 700, margin: '0 auto 14px'
                  }}
                >
                  {user.firstName?.[0]?.toUpperCase() || user.username?.[0]?.toUpperCase() || 'U'}
                </div>

                <h5 className="fw-bold mb-0">
                  {user.firstName} {user.lastName}
                </h5>
                <p className="text-muted small mb-2">@{user.username}</p>

                <div className="d-flex justify-content-center gap-2 mb-3">
                  {user.roles?.map(role => (
                    <Badge
                      key={role}
                      bg={role === 'ROLE_ADMIN' ? 'danger' : 'primary'}
                    >
                      {role.replace('ROLE_', '')}
                    </Badge>
                  ))}
                </div>

                <div className="d-flex align-items-center justify-content-center gap-1 small">
                  <span
                    style={{
                      width: 8, height: 8, borderRadius: '50%',
                      background: statusColor, display: 'inline-block'
                    }}
                  />
                  <span style={{ color: statusColor }} className="fw-semibold">
                    {statusLabel}
                  </span>
                </div>
              </Card.Body>
            </Card>
          </Col>

          {/* Account Details Card */}
          <Col lg={8}>
            <Card className="border-0 shadow-sm h-100">
              <Card.Body>
                <h6 className="fw-bold text-muted text-uppercase mb-3" style={{ fontSize: '0.7rem', letterSpacing: 1 }}>
                  Account Information
                </h6>

                <Row className="g-3">
                  <Col sm={6}>
                    <div className="d-flex align-items-start gap-2">
                      <Mail size={16} className="text-muted mt-1 flex-shrink-0" />
                      <div>
                        <div className="small text-muted">Email</div>
                        <div className="fw-semibold small">{user.email}</div>
                      </div>
                    </div>
                  </Col>

                  <Col sm={6}>
                    <div className="d-flex align-items-start gap-2">
                      <User size={16} className="text-muted mt-1 flex-shrink-0" />
                      <div>
                        <div className="small text-muted">User ID</div>
                        <div className="fw-semibold small">#{user.id}</div>
                      </div>
                    </div>
                  </Col>

                  <Col sm={6}>
                    <div className="d-flex align-items-start gap-2">
                      <Calendar size={16} className="text-muted mt-1 flex-shrink-0" />
                      <div>
                        <div className="small text-muted">Member Since</div>
                        <div className="fw-semibold small">
                          {user.createdAt
                            ? new Date(user.createdAt).toLocaleDateString('en-US', {
                                year: 'numeric', month: 'long', day: 'numeric'
                              })
                            : '—'}
                        </div>
                      </div>
                    </div>
                  </Col>

                  <Col sm={6}>
                    <div className="d-flex align-items-start gap-2">
                      <Shield size={16} className="text-muted mt-1 flex-shrink-0" />
                      <div>
                        <div className="small text-muted">Email Verified</div>
                        <div className="fw-semibold small d-flex align-items-center gap-1">
                          {user.emailVerified
                            ? <><CheckCircle size={14} color="#198754" /> Verified</>
                            : <><XCircle size={14} color="#dc3545" /> Not Verified</>}
                        </div>
                      </div>
                    </div>
                  </Col>

                  {user.phoneNumber && (
                    <Col sm={6}>
                      <div className="d-flex align-items-start gap-2">
                        <span className="text-muted mt-1 flex-shrink-0" style={{ fontSize: 16 }}>📞</span>
                        <div>
                          <div className="small text-muted">Phone</div>
                          <div className="fw-semibold small">{user.phoneNumber}</div>
                        </div>
                      </div>
                    </Col>
                  )}
                </Row>

                <hr className="my-3" />

                <Link to="/settings" className="btn btn-primary btn-sm d-inline-flex align-items-center gap-1">
                  <Settings size={14} /> Update Profile
                </Link>
              </Card.Body>
            </Card>
          </Col>

          {/* Quick Actions */}
          <Col lg={12}>
            <Card className="border-0 shadow-sm">
              <Card.Body>
                <h6 className="fw-bold text-muted text-uppercase mb-3" style={{ fontSize: '0.7rem', letterSpacing: 1 }}>
                  Quick Actions
                </h6>
                <Row className="g-2">
                  <Col xs={6} sm={3}>
                    <Link to="/orders" className="btn btn-outline-primary w-100 d-flex align-items-center justify-content-center gap-2 py-2">
                      <ClipboardList size={16} /> My Orders
                    </Link>
                  </Col>
                  <Col xs={6} sm={3}>
                    <Link to="/cart" className="btn btn-outline-dark w-100 d-flex align-items-center justify-content-center gap-2 py-2">
                      <ShoppingCart size={16} /> View Cart
                    </Link>
                  </Col>
                  <Col xs={6} sm={3}>
                    <Link to="/payment-history" className="btn btn-outline-secondary w-100 d-flex align-items-center justify-content-center gap-2 py-2">
                      <CreditCard size={16} /> Payments
                    </Link>
                  </Col>
                  <Col xs={6} sm={3}>
                    <Link to="/" className="btn btn-outline-success w-100 d-flex align-items-center justify-content-center gap-2 py-2">
                      <ShoppingBag size={16} /> Shop
                    </Link>
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

export default ProfilePage
