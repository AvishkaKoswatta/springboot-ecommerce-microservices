import React, { useState } from 'react'
import { Container, Form, Button, InputGroup } from 'react-bootstrap'
import { useSearchParams, useNavigate, Link } from 'react-router-dom'
import { authService } from '../../api/authService'
import { KeyRound, Eye, EyeOff, CheckCircle, ArrowLeft } from 'lucide-react'
import toast from 'react-hot-toast'

const PASSWORD_REGEX = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/

export const ResetPasswordPage = () => {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const token = searchParams.get('token') || ''

  const [form, setForm] = useState({ newPassword: '', confirmPassword: '' })
  const [errors, setErrors] = useState({})
  const [showNew, setShowNew] = useState(false)
  const [showConfirm, setShowConfirm] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const [success, setSuccess] = useState(false)

  const validate = () => {
    const e = {}
    if (!token) e.token = 'Reset token is missing. Please use the link from your email.'
    if (!form.newPassword) {
      e.newPassword = 'New password is required'
    } else if (!PASSWORD_REGEX.test(form.newPassword)) {
      e.newPassword = 'Must be 8+ chars with uppercase, lowercase, digit & special character'
    }
    if (!form.confirmPassword) {
      e.confirmPassword = 'Please confirm your password'
    } else if (form.newPassword !== form.confirmPassword) {
      e.confirmPassword = 'Passwords do not match'
    }
    return e
  }

  const handleChange = (e) => {
    const { name, value } = e.target
    setForm(prev => ({ ...prev, [name]: value }))
    setErrors(prev => ({ ...prev, [name]: '' }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    const errs = validate()
    if (Object.keys(errs).length > 0) { setErrors(errs); return }

    setIsLoading(true)
    try {
      await authService.resetPassword(token, form.newPassword)
      setSuccess(true)
      toast.success('Password reset successfully!')
    } catch (err) {
      const msg = err?.message || 'Reset failed. The link may be expired or already used.'
      toast.error(msg)
      setErrors({ submit: msg })
    } finally {
      setIsLoading(false)
    }
  }

  if (!token) {
    return (
      <Container className="mt-5">
        <div className="row justify-content-center">
          <div className="col-md-5">
            <div className="card p-4 shadow-sm border-0 text-center">
              <KeyRound size={48} color="#dc3545" className="mb-3 mx-auto" />
              <h5 className="fw-bold">Invalid Reset Link</h5>
              <p className="text-muted small">
                This link is missing a token. Please use the link from your reset email.
              </p>
              <Link to="/forgot-password" className="btn btn-primary mt-2">
                Request New Link
              </Link>
            </div>
          </div>
        </div>
      </Container>
    )
  }

  return (
    <Container className="mt-5">
      <div className="row justify-content-center">
        <div className="col-md-5">
          <div className="card p-4 shadow-sm border-0">

            <Link
              to="/login"
              className="d-inline-flex align-items-center gap-1 text-muted text-decoration-none mb-4 small"
            >
              <ArrowLeft size={14} /> Back to Login
            </Link>

            {!success ? (
              <>
                <div className="text-center mb-4">
                  <div
                    style={{
                      width: 56, height: 56, borderRadius: '50%',
                      background: '#fff3cd', display: 'flex',
                      alignItems: 'center', justifyContent: 'center',
                      margin: '0 auto 12px'
                    }}
                  >
                    <KeyRound size={24} color="#fd7e14" />
                  </div>
                  <h4 className="fw-bold mb-1">Reset Your Password</h4>
                  <p className="text-muted small">Enter your new password below.</p>
                </div>

                <Form onSubmit={handleSubmit}>
                  {errors.submit && (
                    <div className="alert alert-danger py-2 small">{errors.submit}</div>
                  )}

                  <Form.Group className="mb-3">
                    <Form.Label>New Password</Form.Label>
                    <InputGroup>
                      <Form.Control
                        type={showNew ? 'text' : 'password'}
                        name="newPassword"
                        value={form.newPassword}
                        onChange={handleChange}
                        isInvalid={!!errors.newPassword}
                        placeholder="New password"
                        autoFocus
                      />
                      <Button
                        variant="outline-secondary"
                        onClick={() => setShowNew(v => !v)}
                        tabIndex={-1}
                      >
                        {showNew ? <EyeOff size={16} /> : <Eye size={16} />}
                      </Button>
                      <Form.Control.Feedback type="invalid">
                        {errors.newPassword}
                      </Form.Control.Feedback>
                    </InputGroup>
                    <Form.Text className="text-muted" style={{ fontSize: '0.75rem' }}>
                      8+ chars · uppercase · lowercase · digit · special (@$!%*?&)
                    </Form.Text>
                  </Form.Group>

                  <Form.Group className="mb-4">
                    <Form.Label>Confirm Password</Form.Label>
                    <InputGroup>
                      <Form.Control
                        type={showConfirm ? 'text' : 'password'}
                        name="confirmPassword"
                        value={form.confirmPassword}
                        onChange={handleChange}
                        isInvalid={!!errors.confirmPassword}
                        placeholder="Confirm password"
                      />
                      <Button
                        variant="outline-secondary"
                        onClick={() => setShowConfirm(v => !v)}
                        tabIndex={-1}
                      >
                        {showConfirm ? <EyeOff size={16} /> : <Eye size={16} />}
                      </Button>
                      <Form.Control.Feedback type="invalid">
                        {errors.confirmPassword}
                      </Form.Control.Feedback>
                    </InputGroup>
                  </Form.Group>

                  <Button type="submit" variant="primary" className="w-100" disabled={isLoading}>
                    {isLoading ? 'Resetting...' : 'Reset Password'}
                  </Button>
                </Form>
              </>
            ) : (
              <div className="text-center py-3">
                <CheckCircle size={48} color="#198754" className="mb-3" />
                <h5 className="fw-bold">Password Reset!</h5>
                <p className="text-muted small mb-4">
                  Your password has been updated. You can now log in with your new credentials.
                </p>
                <Button variant="primary" className="w-100" onClick={() => navigate('/login')}>
                  Go to Login
                </Button>
              </div>
            )}
          </div>
        </div>
      </div>
    </Container>
  )
}

export default ResetPasswordPage
