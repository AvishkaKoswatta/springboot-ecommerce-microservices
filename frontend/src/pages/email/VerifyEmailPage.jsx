import React, { useEffect, useState } from 'react'
import { Container, Card, Spinner, Alert, Button } from 'react-bootstrap'
import { useSearchParams, useNavigate } from 'react-router-dom'
import { authService } from '../../api/authService'
import toast from 'react-hot-toast'

export const VerifyEmailPage = () => {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()

  const token = searchParams.get('token')

  const [status, setStatus] = useState('loading') 
  // loading | success | error

  const [message, setMessage] = useState('')

  useEffect(() => {
    const verifyEmail = async () => {
      if (!token) {
        setStatus('error')
        setMessage('Invalid verification link (missing token)')
        return
      }

      try {
        const res = await authService.verifyEmail(token)

        // your backend returns ApiResponse
        const response = res.data

        if (response.success) {
          setStatus('success')
          setMessage(response.message || 'Email verified successfully')
          toast.success('Email verified!')
        } else {
          setStatus('error')
          setMessage(response.message || 'Verification failed')
        }
      } catch (error) {
        setStatus('error')
        setMessage(
          error?.message || 'Something went wrong during verification'
        )
      }
    }

    verifyEmail()
  }, [token])

  return (
    <Container className="d-flex justify-content-center align-items-center mt-5">
      <Card style={{ width: '500px' }} className="p-4 text-center shadow">
        <h3 className="mb-3">Email Verification</h3>

        {status === 'loading' && (
          <>
            <Spinner animation="border" className="mb-3" />
            <p>Verifying your email...</p>
          </>
        )}

        {status === 'success' && (
          <>
            <Alert variant="success">{message}</Alert>
            <Button onClick={() => navigate('/login')}>
              Go to Login
            </Button>
          </>
        )}

        {status === 'error' && (
          <>
            <Alert variant="danger">{message}</Alert>
            <Button onClick={() => navigate('/register')}>
              Back to Register
            </Button>
          </>
        )}
      </Card>
    </Container>
  )
}

export default VerifyEmailPage