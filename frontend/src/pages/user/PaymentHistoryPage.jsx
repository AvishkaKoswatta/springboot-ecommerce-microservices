import React, { useState, useEffect } from 'react'
import {
  Container,
  Table,
  Badge,
  Button,
  Pagination,
  Row,
  Col,
} from 'react-bootstrap'
import { useNavigate } from 'react-router-dom'
import { paymentService } from '../../api/paymentService'
import LoadingSpinner from '../../components/LoadingSpinner'
import toast from 'react-hot-toast'

const statusColors = {
  PENDING:   'warning',
  INITIATED: 'info',
  SUCCESS:   'success',
  FAILED:    'danger',
  REFUNDED:  'secondary',
  EXPIRED:   'dark',
}

const methodLabels = {
  CREDIT_CARD:      'Credit Card',
  NET_BANKING:      'Net Banking',
  UPI:              'UPI',
  CASH_ON_DELIVERY: 'Cash on Delivery',
}

export const PaymentHistoryPage = () => {
  const [payments, setPayments] = useState([])
  const [isLoading, setIsLoading] = useState(false)
  const [currentPage, setCurrentPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [pageSize] = useState(10)

  const navigate = useNavigate()

  useEffect(() => {
    fetchPayments(0)
  }, [])

  const fetchPayments = async (page) => {
    setIsLoading(true)
    try {
      const response = await paymentService.getMyPayments(page, pageSize)
      const data = response?.data ?? response ?? {}
      const content = Array.isArray(data.content) ? data.content : []
      setPayments(content)
      setTotalPages(data.totalPages || 0)
      setCurrentPage(page)
    } catch (err) {
      console.error(err)
      toast.error('Failed to load payment history.')
      setPayments([])
    } finally {
      setIsLoading(false)
    }
  }

  if (isLoading && payments.length === 0) return <LoadingSpinner />

  if (!isLoading && payments.length === 0) {
    return (
      <Container className="py-5 text-center">
        <h4>No payment history found</h4>
        <Button onClick={() => navigate('/')}>Continue Shopping</Button>
      </Container>
    )
  }

  return (
    <Container className="py-5">
      <h2 className="mb-4">Payment History</h2>

      <div className="card">
        <div className="table-responsive">
          <Table hover className="mb-0">
            <thead>
              <tr>
                <th>Reference</th>
                <th>Order</th>
                <th>Date</th>
                <th>Method</th>
                <th>Amount</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {payments.map((payment) => (
                <tr key={payment.id}>
                  <td>
                    <span className="text-monospace small">
                      {payment.paymentReference}
                    </span>
                  </td>

                  <td>
                    <strong>{payment.orderNumber}</strong>
                  </td>

                  <td>
                    {payment.createdAt
                      ? new Date(payment.createdAt).toLocaleDateString()
                      : '-'}
                  </td>

                  <td>{methodLabels[payment.paymentMethod] || payment.paymentMethod}</td>

                  <td className="fw-bold">${Number(payment.amount).toFixed(2)}</td>

                  <td>
                    <Badge bg={statusColors[payment.status] || 'secondary'}>
                      {payment.status}
                    </Badge>
                  </td>

                  <td>
                    {payment.orderId && (
                      <Button
                        size="sm"
                        variant="outline-info"
                        onClick={() => navigate(`/order/${payment.orderId}`)}
                      >
                        View Order
                      </Button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        </div>
      </div>

      {totalPages > 1 && (
        <Row className="mt-4">
          <Col className="d-flex justify-content-center">
            <Pagination>
              {Array.from({ length: totalPages }, (_, i) => (
                <Pagination.Item
                  key={i}
                  active={i === currentPage}
                  onClick={() => fetchPayments(i)}
                >
                  {i + 1}
                </Pagination.Item>
              ))}
            </Pagination>
          </Col>
        </Row>
      )}
    </Container>
  )
}

export default PaymentHistoryPage
