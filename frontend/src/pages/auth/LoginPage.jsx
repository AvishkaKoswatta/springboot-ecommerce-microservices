import React, { useState } from 'react'
import { Container, Form, Button } from 'react-bootstrap'
import { useNavigate, Link } from 'react-router-dom'
import { authService } from '../../api/authService'
import { useAuth } from '../../context/AuthContext'
import toast from 'react-hot-toast'

export const LoginPage = () => {
  const [formData, setFormData] = useState({ email: '', password: '' })
  const [errors, setErrors] = useState({})
  const [isLoading, setIsLoading] = useState(false)
  const navigate = useNavigate()
  const { login } = useAuth()

  const validate = () => {
    const e = {}
    if (!formData.email.trim()) e.email = 'Email is required'
    if (!formData.password) e.password = 'Password is required'
    return e
  }

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
    setErrors(prev => ({ ...prev, [name]: '', submit: '' }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    const errs = validate()
    if (Object.keys(errs).length > 0) { setErrors(errs); return }

    setIsLoading(true)
    try {
      const res = await authService.login({
        emailOrUsername: formData.email,
        password: formData.password,
      })

      const user = res.data?.user
      const token = res.data?.accessToken

      if (!user || !token) throw new Error('Invalid response from server')

      login(user, token)

      if (user.roles?.includes('ROLE_ADMIN')) {
        navigate('/admin/dashboard')
      } else {
        navigate('/')
      }
    } catch (error) {
      const msg = error?.message || 'Invalid email or password'
      toast.error(msg)
      setErrors({ submit: msg })
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <Container className="mt-5">
      <div className="row justify-content-center">
        <div className="col-md-6">
          <div className="card p-4 shadow-sm border-0">
            <h2 className="mb-4 text-center fw-bold">Login to Your Account</h2>

            <Form onSubmit={handleSubmit}>
              {errors.submit && (
                <div className="alert alert-danger mb-3 py-2 small">{errors.submit}</div>
              )}

              <Form.Group className="mb-3">
                <Form.Label>Email or Username</Form.Label>
                <Form.Control
                  type="text"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  isInvalid={!!errors.email}
                  placeholder="Enter email or username"
                  autoFocus
                />
                <Form.Control.Feedback type="invalid">{errors.email}</Form.Control.Feedback>
              </Form.Group>

              <Form.Group className="mb-1">
                <Form.Label>Password</Form.Label>
                <Form.Control
                  type="password"
                  name="password"
                  value={formData.password}
                  onChange={handleChange}
                  isInvalid={!!errors.password}
                  placeholder="Enter password"
                />
                <Form.Control.Feedback type="invalid">{errors.password}</Form.Control.Feedback>
              </Form.Group>

              {/* Forgot Password link */}
              <div className="text-end mb-3">
                <Link to="/forgot-password" className="small text-decoration-none">
                  Forgot password?
                </Link>
              </div>

              <Button variant="primary" type="submit" className="w-100" disabled={isLoading}>
                {isLoading ? 'Logging in...' : 'Login'}
              </Button>
            </Form>

            <p className="text-center mt-3 mb-0 small">
              Don't have an account?{' '}
              <Link to="/register" className="text-decoration-none">Register here</Link>
            </p>
          </div>
        </div>
      </div>
    </Container>
  )
}

export default LoginPage
