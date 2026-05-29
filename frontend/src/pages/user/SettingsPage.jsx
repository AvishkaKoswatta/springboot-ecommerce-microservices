import React, { useState, useEffect } from 'react'
import {
  Container, Row, Col, Card, Form, Button,
  Nav, InputGroup, Alert
} from 'react-bootstrap'
import { useAuth } from '../../context/AuthContext'
import { userService } from '../../api/userService'
import LoadingSpinner from '../../components/LoadingSpinner'
import {
  User, Lock, Eye, EyeOff, Save, ArrowLeft
} from 'lucide-react'
import { Link } from 'react-router-dom'
import toast from 'react-hot-toast'

const PASSWORD_REGEX = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/

// ─── Profile Tab ────────────────────────────────────────────────────────────

const ProfileTab = ({ user, setUser }) => {
  const [form, setForm] = useState({
    username: user?.username || '',
    firstName: user?.firstName || '',
    lastName: user?.lastName || '',
    phoneNumber: user?.phoneNumber || '',
    profilePictureUrl: user?.profilePictureUrl || '',
  })
  const [loading, setLoading] = useState(false)
  const [dirty, setDirty] = useState(false)

  useEffect(() => {
    if (user) {
      setForm({
        username: user.username || '',
        firstName: user.firstName || '',
        lastName: user.lastName || '',
        phoneNumber: user.phoneNumber || '',
        profilePictureUrl: user.profilePictureUrl || '',
      })
      setDirty(false)
    }
  }, [user])

  const handleChange = (e) => {
    setForm(prev => ({ ...prev, [e.target.name]: e.target.value }))
    setDirty(true)
  }

  const handleReset = () => {
    setForm({
      username: user?.username || '',
      firstName: user?.firstName || '',
      lastName: user?.lastName || '',
      phoneNumber: user?.phoneNumber || '',
      profilePictureUrl: user?.profilePictureUrl || '',
    })
    setDirty(false)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      const updated = await userService.updateProfile(form)
      if (setUser) {
        setUser(updated)
        // Keep localStorage in sync
        localStorage.setItem('user', JSON.stringify(updated))
      }
      setDirty(false)
      toast.success('Profile updated successfully')
    } catch (err) {
      toast.error(err?.message || 'Failed to update profile')
    } finally {
      setLoading(false)
    }
  }

  return (
    <Form onSubmit={handleSubmit}>
      <h6 className="fw-bold text-muted text-uppercase mb-3" style={{ fontSize: '0.7rem', letterSpacing: 1 }}>
        Personal Information
      </h6>

      <Row className="mb-3">
        <Col sm={6}>
          <Form.Group>
            <Form.Label className="small fw-semibold">First Name</Form.Label>
            <Form.Control
              name="firstName"
              value={form.firstName}
              onChange={handleChange}
              placeholder="John"
            />
          </Form.Group>
        </Col>
        <Col sm={6}>
          <Form.Group>
            <Form.Label className="small fw-semibold">Last Name</Form.Label>
            <Form.Control
              name="lastName"
              value={form.lastName}
              onChange={handleChange}
              placeholder="Doe"
            />
          </Form.Group>
        </Col>
      </Row>

      <Form.Group className="mb-3">
        <Form.Label className="small fw-semibold">Username</Form.Label>
        <Form.Control
          name="username"
          value={form.username}
          onChange={handleChange}
          placeholder="johndoe"
        />
        <Form.Text className="text-muted" style={{ fontSize: '0.75rem' }}>
          Must be unique. Changing this affects your login.
        </Form.Text>
      </Form.Group>

      <Form.Group className="mb-3">
        <Form.Label className="small fw-semibold">Email Address</Form.Label>
        <Form.Control
          value={user?.email || ''}
          disabled
          className="bg-light"
        />
        <Form.Text className="text-muted" style={{ fontSize: '0.75rem' }}>
          Email cannot be changed. Contact support if needed.
        </Form.Text>
      </Form.Group>

      <Form.Group className="mb-3">
        <Form.Label className="small fw-semibold">Phone Number</Form.Label>
        <Form.Control
          name="phoneNumber"
          value={form.phoneNumber}
          onChange={handleChange}
          placeholder="+1 234 567 8900"
        />
      </Form.Group>

      <Form.Group className="mb-4">
        <Form.Label className="small fw-semibold">Profile Picture URL</Form.Label>
        <Form.Control
          name="profilePictureUrl"
          value={form.profilePictureUrl}
          onChange={handleChange}
          placeholder="https://..."
        />
      </Form.Group>

      <div className="d-flex gap-2">
        <Button type="submit" variant="primary" disabled={loading || !dirty}>
          <Save size={15} className="me-1" />
          {loading ? 'Saving...' : 'Save Changes'}
        </Button>
        {dirty && (
          <Button type="button" variant="outline-secondary" onClick={handleReset}>
            Discard
          </Button>
        )}
      </div>
    </Form>
  )
}

// ─── Password Tab ────────────────────────────────────────────────────────────

const PasswordTab = () => {
  const [form, setForm] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  })
  const [errors, setErrors] = useState({})
  const [showCurrent, setShowCurrent] = useState(false)
  const [showNew, setShowNew] = useState(false)
  const [showConfirm, setShowConfirm] = useState(false)
  const [loading, setLoading] = useState(false)

  const handleChange = (e) => {
    const { name, value } = e.target
    setForm(prev => ({ ...prev, [name]: value }))
    setErrors(prev => ({ ...prev, [name]: '', submit: '' }))
  }

  const validate = () => {
    const e = {}
    if (!form.currentPassword) e.currentPassword = 'Current password is required'
    if (!form.newPassword) {
      e.newPassword = 'New password is required'
    } else if (!PASSWORD_REGEX.test(form.newPassword)) {
      e.newPassword = 'Must be 8+ chars with uppercase, lowercase, digit & special character (@$!%*?&)'
    }
    if (!form.confirmPassword) {
      e.confirmPassword = 'Please confirm your password'
    } else if (form.newPassword !== form.confirmPassword) {
      e.confirmPassword = 'Passwords do not match'
    }
    return e
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    const errs = validate()
    if (Object.keys(errs).length > 0) { setErrors(errs); return }

    setLoading(true)
    try {
      await userService.changePassword(
        form.currentPassword,
        form.newPassword,
        form.confirmPassword
      )
      toast.success('Password changed successfully')
      setForm({ currentPassword: '', newPassword: '', confirmPassword: '' })
      setErrors({})
    } catch (err) {
      const msg = err?.message || 'Failed to change password'
      toast.error(msg)
      setErrors({ submit: msg })
    } finally {
      setLoading(false)
    }
  }

  const PasswordField = ({ name, label, show, onToggle, placeholder }) => (
    <Form.Group className="mb-3">
      <Form.Label className="small fw-semibold">{label}</Form.Label>
      <InputGroup>
        <Form.Control
          type={show ? 'text' : 'password'}
          name={name}
          value={form[name]}
          onChange={handleChange}
          isInvalid={!!errors[name]}
          placeholder={placeholder}
        />
        <Button variant="outline-secondary" onClick={onToggle} tabIndex={-1}>
          {show ? <EyeOff size={15} /> : <Eye size={15} />}
        </Button>
        <Form.Control.Feedback type="invalid">{errors[name]}</Form.Control.Feedback>
      </InputGroup>
    </Form.Group>
  )

  return (
    <Form onSubmit={handleSubmit}>
      <h6 className="fw-bold text-muted text-uppercase mb-3" style={{ fontSize: '0.7rem', letterSpacing: 1 }}>
        Change Password
      </h6>

      {errors.submit && (
        <Alert variant="danger" className="py-2 small">{errors.submit}</Alert>
      )}

      <PasswordField
        name="currentPassword"
        label="Current Password"
        show={showCurrent}
        onToggle={() => setShowCurrent(v => !v)}
        placeholder="Your current password"
      />

      <hr className="my-3" />

      <PasswordField
        name="newPassword"
        label="New Password"
        show={showNew}
        onToggle={() => setShowNew(v => !v)}
        placeholder="New password"
      />
      <Form.Text className="text-muted d-block mb-3" style={{ fontSize: '0.75rem', marginTop: -8 }}>
        8+ characters · uppercase · lowercase · digit · special character (@$!%*?&)
      </Form.Text>

      <PasswordField
        name="confirmPassword"
        label="Confirm New Password"
        show={showConfirm}
        onToggle={() => setShowConfirm(v => !v)}
        placeholder="Confirm new password"
      />

      <Button type="submit" variant="primary" disabled={loading}>
        <Lock size={15} className="me-1" />
        {loading ? 'Updating...' : 'Update Password'}
      </Button>
    </Form>
  )
}

// ─── Main SettingsPage ───────────────────────────────────────────────────────

export const SettingsPage = () => {
  const { user, setUser } = useAuth()
  const [activeTab, setActiveTab] = useState('profile')

  if (!user) return <LoadingSpinner />

  return (
    <div style={{ background: '#f6f7fb', minHeight: '100vh' }}>
      <Container className="py-5">

        {/* Header */}
        <div className="mb-4 d-flex align-items-center gap-3">
          <Link to="/profile" className="text-muted text-decoration-none d-flex align-items-center gap-1 small">
            <ArrowLeft size={16} /> Back to Profile
          </Link>
        </div>
        <div className="mb-4">
          <h2 className="fw-bold mb-1">Account Settings</h2>
          <p className="text-muted mb-0">Manage your profile and security preferences.</p>
        </div>

        <Row>
          {/* Sidebar Nav */}
          <Col lg={3} className="mb-4">
            <Card className="border-0 shadow-sm">
              <Card.Body className="p-2">
                <Nav className="flex-column gap-1">
                  <Nav.Item>
                    <button
                      className={`btn btn-sm w-100 text-start d-flex align-items-center gap-2 ${
                        activeTab === 'profile'
                          ? 'btn-primary'
                          : 'btn-light text-dark'
                      }`}
                      onClick={() => setActiveTab('profile')}
                    >
                      <User size={15} />
                      Profile
                    </button>
                  </Nav.Item>
                  <Nav.Item>
                    <button
                      className={`btn btn-sm w-100 text-start d-flex align-items-center gap-2 ${
                        activeTab === 'password'
                          ? 'btn-primary'
                          : 'btn-light text-dark'
                      }`}
                      onClick={() => setActiveTab('password')}
                    >
                      <Lock size={15} />
                      Password
                    </button>
                  </Nav.Item>
                </Nav>
              </Card.Body>
            </Card>

            {/* Account info summary */}
            <Card className="border-0 shadow-sm mt-3">
              <Card.Body>
                <div className="text-center mb-2">
                  <div
                    style={{
                      width: 60, height: 60, borderRadius: '50%',
                      background: '#0d6efd', color: 'white',
                      display: 'flex', alignItems: 'center',
                      justifyContent: 'center', fontSize: 22,
                      margin: '0 auto 10px', fontWeight: 700
                    }}
                  >
                    {user.firstName?.[0]?.toUpperCase() || user.username?.[0]?.toUpperCase() || 'U'}
                  </div>
                  <div className="fw-bold">{user.firstName} {user.lastName}</div>
                  <div className="text-muted small">{user.email}</div>
                </div>
              </Card.Body>
            </Card>
          </Col>

          {/* Main Content */}
          <Col lg={9}>
            <Card className="border-0 shadow-sm">
              <Card.Body className="p-4">
                {activeTab === 'profile' && (
                  <ProfileTab user={user} setUser={setUser} />
                )}
                {activeTab === 'password' && <PasswordTab />}
              </Card.Body>
            </Card>
          </Col>
        </Row>

      </Container>
    </div>
  )
}

export default SettingsPage
