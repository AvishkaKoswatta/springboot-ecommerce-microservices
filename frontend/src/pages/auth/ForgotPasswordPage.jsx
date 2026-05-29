import React, { useState } from 'react'
import { Container, Form, Button } from 'react-bootstrap'
import { Link } from 'react-router-dom'
import { authService } from '../../api/authService'
import { Mail, ArrowLeft, CheckCircle } from 'lucide-react'
import toast from 'react-hot-toast'

export const ForgotPasswordPage = () => {
  const [email, setEmail] = useState('')
  const [error, setError] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [submitted, setSubmitted] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')

    if (!email.trim()) {
      setError('Email is required')
      return
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      setError('Please enter a valid email address')
      return
    }

    setIsLoading(true)
    try {
      await authService.forgotPassword(email.trim())
      setSubmitted(true)
      toast.success('Reset link sent if account exists')
    } catch (err) {
      // Always show success to prevent email enumeration (mirrors backend behaviour)
      setSubmitted(true)
      toast.success('Reset link sent if account exists')
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <Container className="mt-5">
      <div className="row justify-content-center">
        <div className="col-md-5">
          <div className="card p-4 shadow-sm border-0">

            {/* Back link */}
            <Link
              to="/login"
              className="d-inline-flex align-items-center gap-1 text-muted text-decoration-none mb-4 small"
            >
              <ArrowLeft size={14} /> Back to Login
            </Link>

            {!submitted ? (
              <>
                <div className="text-center mb-4">
                  <div
                    style={{
                      width: 56, height: 56, borderRadius: '50%',
                      background: '#e8f0fe', display: 'flex',
                      alignItems: 'center', justifyContent: 'center',
                      margin: '0 auto 12px'
                    }}
                  >
                    <Mail size={24} color="#0d6efd" />
                  </div>
                  <h4 className="fw-bold mb-1">Forgot Password?</h4>
                  <p className="text-muted small mb-0">
                    Enter your email and we'll send a reset link.
                  </p>
                </div>

                <Form onSubmit={handleSubmit}>
                  <Form.Group className="mb-3">
                    <Form.Label>Email Address</Form.Label>
                    <Form.Control
                      type="email"
                      value={email}
                      onChange={(e) => { setEmail(e.target.value); setError('') }}
                      isInvalid={!!error}
                      placeholder="you@example.com"
                      autoFocus
                    />
                    <Form.Control.Feedback type="invalid">{error}</Form.Control.Feedback>
                  </Form.Group>

                  <Button
                    type="submit"
                    variant="primary"
                    className="w-100"
                    disabled={isLoading}
                  >
                    {isLoading ? 'Sending...' : 'Send Reset Link'}
                  </Button>
                </Form>
              </>
            ) : (
              <div className="text-center py-3">
                <CheckCircle size={48} color="#198754" className="mb-3" />
                <h5 className="fw-bold">Check Your Inbox</h5>
                <p className="text-muted small mb-4">
                  If <strong>{email}</strong> is registered, a password reset link has been sent.
                  The link expires in <strong>30 minutes</strong>.
                </p>
                <p className="text-muted" style={{ fontSize: '0.8rem' }}>
                  Didn't receive it? Check your spam folder or{' '}
                  <span
                    role="button"
                    className="text-primary"
                    style={{ cursor: 'pointer' }}
                    onClick={() => { setSubmitted(false); setEmail('') }}
                  >
                    try again
                  </span>.
                </p>
                <Link to="/login" className="btn btn-outline-primary mt-2 w-100">
                  Return to Login
                </Link>
              </div>
            )}
          </div>
        </div>
      </div>
    </Container>
  )
}

export default ForgotPasswordPage
