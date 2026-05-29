import React, { useState, useEffect } from 'react'
import {
  Container,
  Row,
  Col,
  Card,
  Form,
  Button,
  Alert,
  Spinner,
  Badge
} from 'react-bootstrap'
import { useParams, useNavigate, useLocation } from 'react-router-dom'
import { paymentService } from '../../api/paymentService'
import { orderService } from '../../api/orderService'
import { useAuth } from '../../context/AuthContext'
import toast from 'react-hot-toast'

const PAYMENT_METHOD_LABELS = {
  CREDIT_CARD: 'Credit Card',
  NET_BANKING: 'Net Banking',
  UPI: 'UPI',
}

export const PaymentPage = () => {
  const { orderId } = useParams()
  const navigate = useNavigate()
  const location = useLocation()
  const { user } = useAuth()

  // ── SAFE STATE INITIALIZATION ─────────────────────────────
  const [order, setOrder] = useState(location.state?.order || null)
  const [paymentRef, setPaymentRef] = useState(location.state?.paymentReference || null)
  const paymentMethod = location.state?.paymentMethod || order?.paymentMethod

  const [isLoadingOrder, setIsLoadingOrder] = useState(!order)
  const [isProcessing, setIsProcessing] = useState(false)
  const [paymentError, setPaymentError] = useState(null)

  const [cardDetails, setCardDetails] = useState({
    cardholderName: '',
    cardNumber: '',
    expiryMonth: '',
    expiryYear: '',
    cvv: '',
  })

  const [upiId, setUpiId] = useState('')
  const [netBankingBank, setNetBankingBank] = useState('')

  // ── FETCH ORDER ONLY IF NEEDED ─────────────────────────────
  useEffect(() => {
    if (!order && orderId) fetchOrder()
  }, [orderId])

  const fetchOrder = async () => {
    setIsLoadingOrder(true)
    try {
      const response = await orderService.getOrderById(orderId)
      const data = response?.data ?? response
      setOrder(data)
    } catch (err) {
      toast.error('Failed to load order')
      navigate('/orders')
    } finally {
      setIsLoadingOrder(false)
    }
  }

  // ── FORM HANDLING ──────────────────────────────────────────
  const handleCardChange = (e) => {
    const { name, value } = e.target

    if (name === 'cardNumber') {
      const digits = value.replace(/\D/g, '').slice(0, 16)
      const formatted = digits.replace(/(.{4})/g, '$1 ').trim()
      setCardDetails((p) => ({ ...p, cardNumber: formatted }))
      return
    }

    if (name === 'cvv') {
      const digits = value.replace(/\D/g, '').slice(0, 4)
      setCardDetails((p) => ({ ...p, cvv: digits }))
      return
    }

    setCardDetails((p) => ({ ...p, [name]: value }))
  }

  // ── BUILD PAYLOAD ──────────────────────────────────────────
  const buildProcessPayload = () => {
    const base = { paymentReference: paymentRef }

    if (paymentMethod === 'UPI') {
      return { ...base, upiId }
    }

    if (paymentMethod === 'NET_BANKING') {
      return { ...base, bankCode: netBankingBank }
    }

    return {
      ...base,
      cardDetails: {
        cardholderName: cardDetails.cardholderName,
        cardNumber: cardDetails.cardNumber.replace(/\s/g, ''),
        expiryMonth: cardDetails.expiryMonth,
        expiryYear: cardDetails.expiryYear,
        cvv: cardDetails.cvv,
      },
    }
  }

  // ── VALIDATION ─────────────────────────────────────────────
  const validateForm = () => {
    if (!paymentRef) {
      setPaymentError('Payment session not ready')
      return false
    }

    if (paymentMethod === 'UPI') {
      if (!upiId.trim()) return toast.error('Enter UPI ID') && false
    }

    if (paymentMethod === 'NET_BANKING') {
      if (!netBankingBank) return toast.error('Select bank') && false
    }

    if (paymentMethod === 'CREDIT_CARD') {
      if (!cardDetails.cardholderName) return toast.error('Enter cardholder name') && false
      if (cardDetails.cardNumber.replace(/\s/g, '').length < 16)
        return toast.error('Invalid card number') && false
      if (!cardDetails.expiryMonth || !cardDetails.expiryYear)
        return toast.error('Enter expiry') && false
      if (cardDetails.cvv.length < 3)
        return toast.error('Invalid CVV') && false
    }

    return true
  }
console.log("INITIATE PAYMENT PAYLOAD:", {
  orderId: order.id,
  orderNumber: order.orderNumber,
  amount: Number(order.totalAmount),
  currency: 'USD',
  paymentMethod: order.paymentMethod,
  userEmail: user?.email,
  paymentReference: order.paymentReference
})
  // ── SUBMIT PAYMENT ──────────────────────────────────────────
  const handleSubmit = async (e) => {
    e.preventDefault()
    setPaymentError(null)

    if (!validateForm()) return

    setIsProcessing(true)
    try {
      const payload = buildProcessPayload()

      const response = await paymentService.processPayment(payload)
      const status = response?.data?.status ?? response?.status

      if (status === 'SUCCESS') {
        toast.success('Payment successful!')
        navigate(`/payment-success/${orderId}`, {
          state: {
            orderNumber: order.orderNumber,
            totalAmount: order.totalAmount,
          },
        })
      } else {
        setPaymentError('Payment failed or not approved')
      }
    } catch (err) {
      setPaymentError(err?.message || 'Payment error')
    } finally {
      setIsProcessing(false)
    }
  }

  // ── LOADING ────────────────────────────────────────────────
  if (isLoadingOrder) {
    return (
      <Container className="py-5 text-center">
        <Spinner animation="border" />
        <p>Loading order...</p>
      </Container>
    )
  }

  if (!order) return null

  const isCOD = paymentMethod === 'CASH_ON_DELIVERY'

  if (isCOD) {
    return (
      <Container className="py-5 text-center">
        <h3>Order Placed</h3>
        <p>COD Order #{order.orderNumber}</p>
        <Button onClick={() => navigate(`/order/${orderId}`)}>
          View Order
        </Button>
      </Container>
    )
  }

  // ── UI ─────────────────────────────────────────────────────
  return (
    <Container className="py-5">
      <Row className="justify-content-center">
        <Col lg={7}>

          <Card className="mb-3 bg-light">
            <Card.Body className="d-flex justify-content-between">
              <div>#{order.orderNumber}</div>
              <Badge>{PAYMENT_METHOD_LABELS[paymentMethod]}</Badge>
              <strong>${order.totalAmount}</strong>
            </Card.Body>
          </Card>

          <Card>
            <Card.Body>

              {paymentError && (
                <Alert variant="danger">{paymentError}</Alert>
              )}

              {!paymentRef && (
                <Alert variant="warning">
                  Payment session missing
                </Alert>
              )}

              <Form onSubmit={handleSubmit}>

                {paymentMethod === 'CREDIT_CARD' && (
                  <>
                    <Form.Control
                      name="cardholderName"
                      placeholder="Card Holder"
                      className="mb-2"
                      onChange={handleCardChange}
                    />
                    <Form.Control
                      name="cardNumber"
                      placeholder="Card Number"
                      className="mb-2"
                      value={cardDetails.cardNumber}
                      onChange={handleCardChange}
                    />
                    <Row>
                      <Col>
                        <Form.Control
                          name="expiryMonth"
                          placeholder="MM"
                          onChange={handleCardChange}
                        />
                      </Col>
                      <Col>
                        <Form.Control
                          name="expiryYear"
                          placeholder="YYYY"
                          onChange={handleCardChange}
                        />
                      </Col>
                      <Col>
                        <Form.Control
                          name="cvv"
                          placeholder="CVV"
                          onChange={handleCardChange}
                        />
                      </Col>
                    </Row>
                  </>
                )}

                {paymentMethod === 'UPI' && (
                  <Form.Control
                    placeholder="UPI ID"
                    onChange={(e) => setUpiId(e.target.value)}
                  />
                )}

                {paymentMethod === 'NET_BANKING' && (
                  <Form.Select onChange={(e) => setNetBankingBank(e.target.value)}>
                    <option value="">Select Bank</option>
                    <option value="SBI">SBI</option>
                    <option value="HDFC">HDFC</option>
                    <option value="BOC">BOC</option>
                  </Form.Select>
                )}

                <Button
                  type="submit"
                  className="w-100 mt-3"
                  disabled={isProcessing || !paymentRef}
                >
                  {isProcessing ? 'Processing...' : `Pay $${order.totalAmount}`}
                </Button>

              </Form>

            </Card.Body>
          </Card>

        </Col>
      </Row>
    </Container>
  )
}

export default PaymentPage