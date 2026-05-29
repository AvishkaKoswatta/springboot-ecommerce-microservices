import React, { useEffect, useState } from 'react'
import { Container, Card, Button, Spinner } from 'react-bootstrap'
import { useParams, useNavigate, useLocation } from 'react-router-dom'
import { orderService } from '../../api/orderService'

export const PaymentSuccessPage = () => {
  const { orderId } = useParams()
  const navigate = useNavigate()
  const location = useLocation()

  // Fast path: info passed via navigation state (no extra API call needed)
  const [order, setOrder] = useState(
    location.state
      ? { orderNumber: location.state.orderNumber, totalAmount: location.state.totalAmount }
      : null
  )
  const [isLoading, setIsLoading] = useState(!order)

  useEffect(() => {
    if (!order && orderId) {
      orderService.getOrderById(orderId)
        .then((res) => {
          const data = res?.data ?? res
          setOrder(data)
        })
        .catch(console.error)
        .finally(() => setIsLoading(false))
    }
  }, [orderId])

  if (isLoading) {
    return (
      <Container className="py-5 text-center">
        <Spinner animation="border" />
      </Container>
    )
  }

  return (
    <Container className="py-5">
      <div className="row justify-content-center">
        <div className="col-md-6">
          <Card className="text-center shadow-sm border-0">
            <Card.Body className="p-5">
              <div style={{ fontSize: 72 }}>✅</div>
              <h2 className="mt-3 text-success">Payment Successful!</h2>

              {order && (
                <>
                  <p className="text-muted mt-2">
                    Order <strong>#{order.orderNumber}</strong> has been confirmed.
                  </p>
                  <p className="fs-5 fw-bold">
                    ${Number(order.totalAmount).toFixed(2)} paid
                  </p>
                </>
              )}

              <p className="text-muted small">
                You will receive a confirmation email shortly.
              </p>

              <div className="d-grid gap-2 mt-4">
                <Button onClick={() => navigate(`/order/${orderId}`)}>
                  View Order Details
                </Button>
                <Button variant="outline-secondary" onClick={() => navigate('/orders')}>
                  My Orders
                </Button>
                <Button variant="outline-primary" onClick={() => navigate('/')}>
                  Continue Shopping
                </Button>
              </div>
            </Card.Body>
          </Card>
        </div>
      </div>
    </Container>
  )
}

export default PaymentSuccessPage
